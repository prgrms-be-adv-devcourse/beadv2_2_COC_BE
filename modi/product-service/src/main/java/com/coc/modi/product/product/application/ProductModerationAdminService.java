package com.coc.modi.product.product.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.coc.modi.kafka.event.ProductModerationRequestedEvent;
import com.coc.modi.kafka.event.ProductModerationResultEvent;
import com.coc.modi.product.outbox.ProductOutboxService;
import com.coc.modi.product.product.application.dto.ProductModerationSummaryResponse;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductModerationStatus;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.exception.ProductConflictException;
import com.coc.modi.product.product.exception.ProductNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductModerationAdminService {

	private static final int SPEC_VALUE_LIMIT = 200;
	private static final String REQUEST_REASON = "ADMIN_REQUEST";
	private static final String OVERRIDE_REASON = "ADMIN_OVERRIDE";

	private final ProductRepository productRepository;
	private final ProductOutboxService productOutboxService;
	private final ProductModerationResultService productModerationResultService;

	@Transactional(readOnly = true)
	public Page<ProductModerationSummaryResponse> getModerationRequests(ProductModerationStatus moderationStatus,
																	   Pageable pageable) {

		Page<Product> products = productRepository.findNonDeletedByModerationStatus(moderationStatus, pageable);

		return products.map(ProductModerationSummaryResponse::from);
	}

	@Transactional
	public void requestModeration(Long productId) {

		Product product = productRepository.findNonDeletedById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));

		if (product.getModerationStatus() != ProductModerationStatus.PENDING) {
			throw new ProductConflictException(
					"모더레이션 요청은 PENDING 상태에서만 가능합니다. status=" + product.getModerationStatus());
		}

		ProductModerationRequestedEvent event = ProductModerationRequestedEvent.of(
				product.getId(),
				product.getSellerId(),
				REQUEST_REASON,
				product.getName(),
				product.getDescription(),
				limitSpecValues(product.getSpecs()),
				extractImageUrls(product)
		);

		productOutboxService.enqueueModerationRequested(product.getId(), event);
	}

	@Transactional
	public void approveModeration(Long productId, String reason) {

		Product product = productRepository.findNonDeletedById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));

		if (product.getModerationStatus() == ProductModerationStatus.CLEAR) {
			return;
		}

		String message = (reason != null && !reason.isBlank()) ? reason : null;
		ProductModerationResultEvent event = ProductModerationResultEvent.of(
				product.getId(),
				ProductModerationStatus.CLEAR.name(),
				1.0d,
				List.of(OVERRIDE_REASON),
				message
		);
		productModerationResultService.handle(event);
	}

	private List<String> limitSpecValues(Map<String, String> specs) {

		if (specs == null || specs.isEmpty()) {
			return List.of();
		}

		return specs.values().stream()
				.filter(Objects::nonNull)
				.map(value -> value.length() > SPEC_VALUE_LIMIT ? value.substring(0, SPEC_VALUE_LIMIT) : value)
				.toList();
	}

	private List<String> extractImageUrls(Product product) {

		if (product.getImages() == null || product.getImages().isEmpty()) {
			return List.of();
		}

		return product.getImages().stream()
				.map(image -> image.getUrl())
				.filter(Objects::nonNull)
				.toList();
	}
}
