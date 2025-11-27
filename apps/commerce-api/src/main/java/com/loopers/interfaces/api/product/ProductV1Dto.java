package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

public class ProductV1Dto {
    public record ProductRequest(
    ) {

    }

    public record ProductResponse(Long productId, String productName, Long brandId, String brandName, Long price, Long likeCount) {
        public static ProductResponse from(ProductInfo productInfo) {
            return new ProductResponse(
                    productInfo.productId(),
                    productInfo.productName(),
                    productInfo.brandId(),
                    productInfo.brandName(),
                    productInfo.price(),
                    productInfo.likeCount()
            );
        }
    }
}
