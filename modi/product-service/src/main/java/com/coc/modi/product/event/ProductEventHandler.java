package com.coc.modi.product.event;

import org.springframework.stereotype.Service;

import com.coc.modi.kafka.event.ProductEmbeddingEvent;
import com.coc.modi.kafka.event.ProductIndexEvent;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.recommendation.embedding.ProductEmbeddingService;
import com.coc.modi.product.search.application.ProductIndexService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventHandler {

	private final ProductRepository productRepository;
	private final ProductIndexService productIndexService;
	private final ProductEmbeddingService productEmbeddingService;

	public void handle(ProductIndexEvent event) {
		try {
			switch (event.action()) {
				case INDEX -> productRepository.findById(event.productId())
						.ifPresentOrElse(
								productIndexService::index,
								() -> log.warn("Kafka 이벤트 건너뜀. reason=product-not-found event=product-index eventId={} productId={} action={}",
										event.eventId(), event.productId(), event.action())
						);
				case DELETE -> productIndexService.deleteById(event.productId());
				default -> log.warn("Kafka 이벤트 건너뜀. reason=unknown-action event=product-index eventId={} productId={} action={}",
						event.eventId(), event.productId(), event.action());
			}
		} catch (Exception ex) {
			log.warn("Kafka 이벤트 처리 실패. event=product-index eventId={} productId={} action={}",
					event.eventId(), event.productId(), event.action(), ex);
		}
	}

	public void handle(ProductEmbeddingEvent event) {
		if (event.action() != ProductEmbeddingEvent.Action.UPDATE) {
			log.warn("Kafka 이벤트 건너뜀. reason=unknown-action event=product-embedding eventId={} productId={} action={}",
					event.eventId(), event.productId(), event.action());
			return;
		}

		try {
			productRepository.findById(event.productId())
					.ifPresentOrElse(
							productEmbeddingService::updateEmbedding,
							() -> log.warn("Kafka 이벤트 건너뜀. reason=product-not-found event=product-embedding eventId={} productId={} action={}",
									event.eventId(), event.productId(), event.action())
					);
		} catch (Exception ex) {
			log.warn("Kafka 이벤트 처리 실패. event=product-embedding eventId={} productId={} action={}",
					event.eventId(), event.productId(), event.action(), ex);
		}
	}
}
