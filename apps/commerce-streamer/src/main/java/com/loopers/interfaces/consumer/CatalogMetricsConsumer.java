package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.domain.event.EventHandledService;
import com.loopers.domain.metrics.ProductMetricsService;
import com.loopers.interfaces.consumer.message.CatalogEventMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogMetricsConsumer {

    private static final String TOPIC = "catalog-events";
    private static final String HANDLER = "catalog-metrics-consumer";

    private final ProductMetricsService productMetricsService;
    private final EventHandledService eventHandledService;

    @KafkaListener(
            topics = TOPIC,
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void consume(
            List<CatalogEventMessage> messages,
            Acknowledgment acknowledgment
    ) {
        if (messages == null || messages.isEmpty()) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            for (CatalogEventMessage message : messages) {
                if (message == null || message.eventId() == null) {
                    continue;
                }
                if (eventHandledService.isHandled(message.eventId())) {
                    log.debug("Skip already handled catalog event eventId={}", message.eventId());
                    continue;
                }
                long version = message.occurredAt() == null
                        ? System.currentTimeMillis()
                        : message.occurredAt().toInstant().toEpochMilli();
                productMetricsService.applyLikeDelta(message.productId(), message.delta(), version);
                eventHandledService.markHandled(
                        message.eventId(),
                        HANDLER,
                        TOPIC,
                        message.eventType(),
                        message.occurredAt()
                );
            }
            acknowledgment.acknowledge();
        } catch (Exception exception) {
            log.error("Failed to process catalog events", exception);
            throw exception;
        }
    }
}
