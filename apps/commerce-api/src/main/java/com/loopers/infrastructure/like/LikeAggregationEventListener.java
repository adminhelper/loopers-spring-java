package com.loopers.infrastructure.like;

import com.loopers.domain.like.event.LikeEvent;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeAggregationEventListener {

    private final ProductRepository productRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LikeEvent.ProductLiked event) {
        try {
            productRepository.incrementLikeCount(event.productId());
        } catch (Exception exception) {
            log.error("집계 처리 실패 - productId={}, type=LIKE", event.productId(), exception);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LikeEvent.ProductUnliked event) {
        try {
            productRepository.decrementLikeCount(event.productId());
        } catch (Exception exception) {
            log.error("집계 처리 실패 - productId={}, type=UNLIKE", event.productId(), exception);
        }
    }
}
