package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProductV1Dto {
    public record ProductRequest(
            Long brandId,
            Integer page,
            Integer size,
            String sort
    ) {
    }

    public record ProductResponse(
            List<ProductResponseItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static ProductResponse from(Page<ProductInfo> pageData) {
            List<ProductResponseItem> items = pageData.getContent().stream()
                    .map(ProductResponseItem::from)
                    .toList();
            return new ProductResponse(
                    items,
                    pageData.getNumber(),
                    pageData.getSize(),
                    pageData.getTotalElements(),
                    pageData.getTotalPages()
            );
        }
    }

    public static record ProductResponseItem(
            Long productId,
            String productName,
            Long brandId,
            String brandName,
            Long price,
            Long likeCount
    ) {
        public static ProductResponseItem from(ProductInfo productInfo) {
            return new ProductResponseItem(
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
