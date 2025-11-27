package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * packageName : com.loopers.interfaces.api.product
 * fileName     : ProdcutV1ApiSpec
 * author      : byeonsungmun
 * date        : 2025. 11. 25.
 * description :
 * ===========================================
 * DATE         AUTHOR       NOTE
 * -------------------------------------------
 * 2025. 11. 25.     byeonsungmun       최초 생성
 */
@Tag(name = "Products V1 API", description = "Product API입니다.")
public interface ProductV1ApiSpec {

    @Operation(
            summary = "상품목록 조회",
            description = "브랜드별 상품 목록을 조회합니다. 페이지네이션 및 정렬을 지원합니다."
    )
    ApiResponse<ProductV1Dto.ProductResponse> gerProducts(
            @Schema(description = "브랜드 ID", example = "1")
            @RequestParam(required = false) Long brandId,

            @Schema(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Schema(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") Integer size,

            @Schema(description = "정렬(latest, price_asc, likes_desc)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort
    );

    ApiResponse<ProductV1Dto.ProductResponse> gerProduct(
            @Schema(name = "productId", description = "브랜드별 조회")
            Long productId
    );

}
