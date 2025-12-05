package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @Override
    @PostMapping
    public ApiResponse<OrderV1Dto.OrderResponse> createOrder(
            @RequestHeader("X-USER-ID") String userId,
            @Valid @RequestBody OrderV1Dto.OrderCreateRequest request
    ) {
        OrderInfo orderInfo = orderFacade.createOrder(request.toCommand(userId));
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(orderInfo));
    }
}
