package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.payment.PgPaymentClient;
import com.loopers.infrastructure.payment.dto.PgPaymentV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;
    private final PgPaymentClient pgPaymentClient;
    private final PaymentService paymentService;

    @Transactional
    public OrderInfo createOrder(CreateOrderCommand command) {

        if (command == null || command.items() == null || command.items().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 정보가 비어있습니다");
        }

        Order order = Order.create(command.userId());

        for (OrderItemCommand itemCommand : command.items()) {

            Product product = productService.getProduct(itemCommand.productId());

            product.decreaseStock(itemCommand.quantity());

            OrderItem orderItem = OrderItem.create(
                    product.getId(),
                    product.getName(),
                    itemCommand.quantity(),
                    product.getPrice());

            order.addOrderItem(orderItem);
            orderItem.setOrder(order);
        }

        long totalAmount = order.getOrderItems().stream()
                .mapToLong(OrderItem::getAmount)
                .sum();

        order.updateTotalAmount(totalAmount);

        Point point = pointService.findPointByUserId(command.userId());
        point.use(totalAmount);

        Order saved = orderService.createOrder(order);

        OrderPaymentCommand paymentCommand = command.payment();
        String orderReference = "order-" + saved.getId();
        String callbackUrl = "http://localhost:8080/api/v1/orders/" + orderReference + "/callback";

        PgPaymentV1Dto.Request pgRequest = new PgPaymentV1Dto.Request(
                orderReference,
                parsePgCardType(paymentCommand.cardType()),
                paymentCommand.cardNo(),
                saved.getTotalAmount(),
                callbackUrl
        );

        Payment payment = Payment.pending(
                saved.getId(),
                command.userId(),
                orderReference,
                paymentCommand.cardType(),
                paymentCommand.cardNo(),
                saved.getTotalAmount()
        );

        try {
            PgPaymentV1Dto.ApiResponse<PgPaymentV1Dto.Response> pgResponse = pgPaymentClient.requestPayment(command.userId(), pgRequest);
            PgPaymentV1Dto.Response pgData = requirePgResponse(pgResponse);
            OrderStatus newStatus = mapOrderStatus(pgData.status());
            if (newStatus == OrderStatus.FAIL) {
                revertOrder(saved, command.userId());
            } else {
                saved.updateStatus(newStatus);
            }

            payment.updateStatus(parsePaymentStatus(pgData.status()), pgData.transactionKey(), pgData.reason());
            paymentService.save(payment);
        } catch (Exception e) {
            revertOrder(saved, command.userId());
            payment.updateStatus(PaymentStatus.FAIL, null, e.getMessage());
            paymentService.save(payment);
            throw e;
        }

        return OrderInfo.from(saved);
    }

    @Transactional
    public void handlePaymentCallback(String orderReference, PgPaymentV1Dto.TransactionStatus status, String transactionKey, String reason) {
        Payment payment = paymentService.findByOrderReference(orderReference)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        Order order = orderService.findById(payment.getOrderId());
        OrderStatus newStatus = mapOrderStatus(status);
        payment.updateStatus(parsePaymentStatus(status), transactionKey, reason);
        paymentService.save(payment);
        if (newStatus == OrderStatus.COMPLETE) {
            order.updateStatus(OrderStatus.COMPLETE);
        } else if (newStatus == OrderStatus.FAIL) {
            revertOrder(order, payment.getUserId());
        }
    }

    @Transactional
    public void syncPayment(Long orderId) {
        Payment payment = paymentService.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        Order order = orderService.findById(orderId);
        PgPaymentV1Dto.ApiResponse<PgPaymentV1Dto.OrderResponse> response = pgPaymentClient.getPayments(payment.getUserId(), payment.getOrderReference());
        PgPaymentV1Dto.OrderResponse data = response != null ? response.data() : null;
        if (data == null || data.transactions() == null || data.transactions().isEmpty()) {
            return;
        }
        PgPaymentV1Dto.TransactionRecord record = data.transactions().get(data.transactions().size() - 1);
        OrderStatus newStatus = mapOrderStatus(record.status());
        payment.updateStatus(parsePaymentStatus(record.status()), record.transactionKey(), record.reason());
        paymentService.save(payment);
        if (newStatus == OrderStatus.FAIL) {
            revertOrder(order, payment.getUserId());
        } else {
            order.updateStatus(newStatus);
        }
    }

    private void revertOrder(Order order, String userId) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = productService.getProduct(item.getProductId());
            product.increaseStock(item.getQuantity());
        }
        Point point = pointService.findPointByUserId(userId);
        if (point != null) {
            point.refund(order.getTotalAmount());
        }
        order.updateStatus(OrderStatus.FAIL);
    }

    private PgPaymentV1Dto.CardType parsePgCardType(String cardType) {
        try {
            return PgPaymentV1Dto.CardType.valueOf(cardType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 카드 타입입니다.");
        }
    }

    private OrderStatus mapOrderStatus(PgPaymentV1Dto.TransactionStatus status) {
        if (status == PgPaymentV1Dto.TransactionStatus.SUCCESS) {
            return OrderStatus.COMPLETE;
        }
        if (status == PgPaymentV1Dto.TransactionStatus.FAILED) {
            return OrderStatus.FAIL;
        }
        return OrderStatus.PENDING;
    }

    private PaymentStatus parsePaymentStatus(PgPaymentV1Dto.TransactionStatus status) {
        if (status == PgPaymentV1Dto.TransactionStatus.SUCCESS) {
            return PaymentStatus.SUCCESS;
        }
        if (status == PgPaymentV1Dto.TransactionStatus.FAILED) {
            return PaymentStatus.FAIL;
        }
        return PaymentStatus.PENDING;
    }

    private PgPaymentV1Dto.Response requirePgResponse(PgPaymentV1Dto.ApiResponse<PgPaymentV1Dto.Response> response) {
        PgPaymentV1Dto.Response data = response != null ? response.data() : null;
        if (data == null) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 응답을 확인할 수 없습니다.");
        }
        return data;
    }
}
