package com.coc.modi.ai.event;

import com.coc.modi.ai.embedding.application.ProductEmbeddingService;
import com.coc.modi.ai.embedding.infrastructure.client.ProductEmbeddingClient;
import com.coc.modi.kafka.event.ProductEmbeddingEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingEventHandler {

    private final ProductEmbeddingClient productEmbeddingClient;
    private final ProductEmbeddingService productEmbeddingService;

    public void handle(ProductEmbeddingEvent event) {
        if (event.action() != ProductEmbeddingEvent.Action.UPDATE) {
            log.warn("Kafka 이벤트 건너뜀. reason=unknown-action event=product-embedding eventId={} productId={} action={}",
                    event.eventId(), event.productId(), event.action());
            return;
        }

        try {
            var target = productEmbeddingClient.getEmbeddingTarget(event.productId());
            if (target == null) {
                log.warn("Kafka 이벤트 건너뜀. reason=product-not-found event=product-embedding eventId={} productId={} action={}",
                        event.eventId(), event.productId(), event.action());
                return;
            }
            productEmbeddingService.updateEmbedding(target);
        } catch (Exception ex) {
            log.warn("Kafka 이벤트 처리 실패. event=product-embedding eventId={} productId={} action={}",
                    event.eventId(), event.productId(), event.action(), ex);
        }
    }
}
