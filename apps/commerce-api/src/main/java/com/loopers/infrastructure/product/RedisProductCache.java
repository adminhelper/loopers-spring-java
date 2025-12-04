package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisProductCache implements ProductCache {

    private static final Duration TTL_LIST = Duration.ofMinutes(10);
    private static final Duration TTL_DETAIL = Duration.ofMinutes(5);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Page<Product>> getProductList(Long brandId, Pageable pageable) {
        String key = listKey(brandId, pageable);
        try {
            Page<Product> cached = (Page<Product>) redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(cached);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void putProductList(Long brandId, Pageable pageable, Page<Product> products) {
        String key = listKey(brandId, pageable);
        try {
            redisTemplate.opsForValue().set(key, products, TTL_LIST);
        } catch (Exception ignored) {
        }
    }

    @Override
    public Optional<Product> getProductDetail(Long productId) {
        String key = detailKey(productId);
        try {
            Product cached = (Product) redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(cached);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void putProductDetail(Long productId, Product product) {
        String key = detailKey(productId);
        try {
            redisTemplate.opsForValue().set(key, product, TTL_DETAIL);
        } catch (Exception ignored) {
        }
    }

    private String listKey(Long brandId, Pageable pageable) {
        String sortkey = pageable.getSort().toString();
        return "product:list:"
                + (brandId == null ? "all" : brandId) + ":"
                + pageable.getPageNumber() + ":"
                + pageable.getPageSize() + ":"
                + sortkey;
    }

    private String detailKey(Long productId) {
        return "product:detail:" + productId;
    }
}
