package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductFacade  productFacade;

    @Override
    public ApiResponse<ProductV1Dto.ProductResponse> getProducts(@RequestParam(required = false) Long brandId,
                                                                 @RequestParam(defaultValue = "0") Integer page,
                                                                 @RequestParam(defaultValue = "20") Integer size,
                                                                 @RequestParam(defaultValue = "latest") String sort) {
        Page<ProductInfo> products = productFacade.getProducts(brandId, page, size, sort);
        ProductV1Dto.ProductResponse response = ProductV1Dto.ProductResponse.from(products);
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<ProductV1Dto.ProductResponse> getProduct(Long productId) {
        return null;
    }
}
