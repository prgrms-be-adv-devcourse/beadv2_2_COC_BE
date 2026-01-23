package com.coc.modi.ai.event;

import com.coc.modi.kafka.event.ProductEmbeddingEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingEventListener {

    private final ProductEmbeddingEventHandler productEmbeddingEventHandler;

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_EMBEDDING_EVENTS,
            groupId = "ai-product-embedding",
            containerFactory = "productEmbeddingKafkaListenerContainerFactory"
    )
    public void onProductEmbeddingEvent(ProductEmbeddingEvent event) {
        if (event == null || event.productId() == null || event.action() == null) {
            log.warn("Kafka 이벤트 건너뜀. reason=missing-data event=product-embedding eventId={} productId={} action={}",
                    event != null ? event.eventId() : null,
                    event != null ? event.productId() : null,
                    event != null ? event.action() : null);
            return;
        }

        log.info("Kafka 이벤트 수신. event=product-embedding topic={} eventId={} productId={} action={}",
                KafkaTopics.PRODUCT_EMBEDDING_EVENTS, event.eventId(), event.productId(), event.action());
        productEmbeddingEventHandler.handle(event);
    }
}
