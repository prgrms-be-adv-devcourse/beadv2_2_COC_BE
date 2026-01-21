package com.coc.modi.product.product.application;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coc.modi.common.NotificationType;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.event.ProductModerationResultEvent;
import com.coc.modi.product.event.KafkaProductEmbeddingEventPublisher;
import com.coc.modi.product.outbox.ProductOutboxService;
import com.coc.modi.product.product.application.support.SellerMemberResolver;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductModerationStatus;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.exception.ProductNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductModerationResultService {

	private final ProductRepository productRepository;
	private final SellerMemberResolver sellerMemberResolver;
	private final ProductOutboxService productOutboxService;
	private final KafkaProductEmbeddingEventPublisher embeddingEventPublisher;

	@Transactional
	public void handle(ProductModerationResultEvent event) {

		if (event == null || event.productId() == null) {
			return;
		}

		ProductModerationStatus status = parseStatus(event.result());
		if (status == null) {
			return;
		}

		Product product = productRepository.findNonDeletedById(event.productId())
				.orElseThrow(() -> new ProductNotFoundException(event.productId()));

		product.updateModerationStatus(status);

		if (status != ProductModerationStatus.CLEAR
				&& status != ProductModerationStatus.REVIEW
				&& status != ProductModerationStatus.BLOCKED) {
			return;
		}

		Long memberId = sellerMemberResolver.getMemberId(product.getSellerId());
		NotificationEvent notification = buildNotification(memberId, product, status, event);
		productOutboxService.enqueueNotificationEvent(product.getId(), notification);

		if (status == ProductModerationStatus.CLEAR) {
			embeddingEventPublisher.publishUpdate(product.getId());
		}
	}

	private ProductModerationStatus parseStatus(String result) {

		if (!StringUtils.hasText(result)) {
			return null;
		}

		try {
			return ProductModerationStatus.valueOf(result);
		} catch (IllegalArgumentException ex) {
			log.warn("Unknown moderation result. result={}", result, ex);
			return null;
		}
	}

	private NotificationEvent buildNotification(Long memberId,
										Product product,
										ProductModerationStatus status,
										ProductModerationResultEvent event) {

		String title;
		String baseContent;
		if (status == ProductModerationStatus.CLEAR) {
			title = "상품 등록 승인";
			baseContent = "상품이 승인되었습니다.";
		} else if (status == ProductModerationStatus.REVIEW) {
			title = "상품 검토 필요";
			baseContent = "상품에 확인이 필요한 내용이 있습니다.";
		} else {
			title = "상품 등록 차단";
			baseContent = "상품이 정책 위반으로 차단되었습니다.";
		}
		String content = baseContent;
		if (StringUtils.hasText(event.message())) {
			content = baseContent + " " + event.message();
		}

		String type;
		if (status == ProductModerationStatus.CLEAR) {
			type = NotificationType.PRODUCT_MODERATION_APPROVED.name();
		} else if (status == ProductModerationStatus.REVIEW) {
			type = NotificationType.PRODUCT_MODERATION_REVIEW.name();
		} else {
			type = NotificationType.PRODUCT_MODERATION_BLOCKED.name();
		}

		return NotificationEvent.of(
				Objects.requireNonNull(memberId),
				type,
				title,
				content,
				"PRODUCT",
				product.getId().toString()
		);
	}
}
