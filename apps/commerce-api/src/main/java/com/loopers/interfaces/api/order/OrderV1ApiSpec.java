package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Order V1 API", description = "Order API 입니다.")
@RequestMapping("/api/v1/orders")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문 생성", description = "상품 주문을 생성합니다.")
    @PostMapping
    ApiResponse<OrderV1Dto.OrderResponse> createOrder(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody OrderV1Dto.OrderCreateRequest request
    );
}
