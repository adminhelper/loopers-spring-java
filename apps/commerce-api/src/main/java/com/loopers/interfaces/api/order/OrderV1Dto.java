package com.loopers.interfaces.api.order;

import com.loopers.application.order.CreateOrderCommand;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderItemCommand;
import com.loopers.application.order.OrderItemInfo;
import com.loopers.domain.order.OrderStatus;

import java.util.List;

public class OrderV1Dto {

    public record OrderCreateRequest(
            List<OrderItemRequest> items
    ) {
        public CreateOrderCommand toCommand(String userId) {
            List<OrderItemCommand> itemCommands = items.stream()
                    .map(OrderItemRequest::toCommand)
                    .toList();
            return new CreateOrderCommand(userId, itemCommands);
        }
    }

    public record OrderItemRequest(
            Long productId,
            Long quantity
    ) {
        private OrderItemCommand toCommand() {
            return new OrderItemCommand(productId, quantity);
        }
    }

    public record OrderResponse(
            Long orderId,
            String userId,
            Long totalAmount,
            OrderStatus status,
            List<OrderItemResponse> items
    ) {
        public static OrderResponse from(OrderInfo info) {
            List<OrderItemResponse> responses = info.items().stream()
                    .map(OrderItemResponse::from)
                    .toList();
            return new OrderResponse(
                    info.orderId(),
                    info.userId(),
                    info.totalAmount(),
                    info.status(),
                    responses
            );
        }
    }

    public record OrderItemResponse(
            Long productId,
            String productName,
            Long quantity,
            Long price,
            Long amount
    ) {
        public static OrderItemResponse from(OrderItemInfo info) {
            return new OrderItemResponse(
                    info.productId(),
                    info.productName(),
                    info.quantity(),
                    info.price(),
                    info.amount()
            );
        }
    }
}
