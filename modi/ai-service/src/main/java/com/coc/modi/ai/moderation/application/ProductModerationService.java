package com.coc.modi.ai.moderation.application;

import java.util.List;

import com.coc.modi.ai.moderation.domain.ProductModerationResult;
import com.coc.modi.ai.moderation.domain.ProductModerationResultRepository;
import com.coc.modi.ai.moderation.outbox.ProductModerationOutboxService;
import com.coc.modi.kafka.event.ProductModerationRequestedEvent;
import com.coc.modi.kafka.event.ProductModerationResultEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductModerationService {

	private final ProductModerationModel moderationModel;
	private final ProductModerationResultRepository resultRepository;
	private final ProductModerationOutboxService outboxService;

	@Transactional
	public void handle(ProductModerationRequestedEvent event) {

		ProductModerationDecisionResult decision = moderationModel.moderate(event);

		String reasons = decision.reasons() == null ? null : String.join(",", decision.reasons());
		ProductModerationResult saved = resultRepository.save(
				ProductModerationResult.create(
						event.productId(),
						decision.decision(),
						decision.score(),
						reasons,
						decision.message(),
						event.eventId(),
						decision.source()
				)
		);

		ProductModerationResultEvent resultEvent = ProductModerationResultEvent.of(
				saved.getProductId(),
				decision.decision().name(),
				decision.score(),
				decision.reasons() == null ? List.of() : decision.reasons(),
				decision.message()
		);

		outboxService.enqueueModerationResult(saved.getProductId(), resultEvent);
	}
}
