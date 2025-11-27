package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductFacade  productFacade;


    @Override
    public ApiResponse<ProductV1Dto.ProductResponse> gerProducts(ProductV1Dto.ProductRequest request) {
        return null;
    }

    @Override
    public ApiResponse<ProductV1Dto.ProductResponse> gerProduct(Long productId) {
        return null;
    }
}
