package com.coc.modi.ai.moderation.application;

import java.util.List;

import com.coc.modi.ai.moderation.domain.ProductModerationDecision;

public record ProductModerationDecisionResult(
		ProductModerationDecision decision,
		Double score,
		List<String> reasons,
		String message,
		String source
) {
}
