package com.coc.modi.ai.moderation.infrastructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.CategoryScores;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.stereotype.Component;

import com.coc.modi.ai.config.ModerationProperties;
import com.coc.modi.ai.moderation.application.ProductModerationDecisionResult;
import com.coc.modi.ai.moderation.application.ProductModerationModel;
import com.coc.modi.ai.moderation.domain.ProductModerationDecision;
import com.coc.modi.kafka.event.ProductModerationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiModerationModel implements ProductModerationModel {

	private static final String SOURCE = "spring-ai-moderation";
	private static final String DEFAULT_MESSAGE = "검토가 필요한 내용이 감지되었습니다.";
	private static final Map<String, String> CATEGORY_MESSAGES = buildCategoryMessages();

	private final ModerationModel moderationModel;
	private final ModerationProperties moderationProperties;

	@Override
	public ProductModerationDecisionResult moderate(ProductModerationRequestedEvent event) {

		String input = buildInput(event);
		Moderation moderation;
		try {
			ModerationResponse response = moderationModel.call(new ModerationPrompt(input));
			moderation = resolveModeration(response);
		} catch (Exception ex) {
			log.warn("Moderation API call failed. productId={}", event.productId(), ex);
			return fallbackResult("MODEL_CALL_FAILED");
		}

		ModerationResult result = resolveModerationResult(moderation);
		if (result == null || (result.getCategories() == null && result.getCategoryScores() == null)) {
			return fallbackResult("EMPTY_RESPONSE");
		}

		Map<String, Boolean> categoryFlags = toCategoryFlags(result.getCategories());
		Map<String, Double> categoryScores = toCategoryScores(result.getCategoryScores());
		if (categoryFlags.isEmpty() && categoryScores.isEmpty()) {
			return fallbackResult("EMPTY_RESPONSE");
		}

		List<String> reasons = extractReasons(categoryFlags, categoryScores);
		double score = resolveScore(categoryScores);
		ProductModerationDecision decision = resolveDecision(score);
		String message = buildMessage(reasons);

		return new ProductModerationDecisionResult(decision, score, reasons, message, SOURCE);
	}

	private double resolveScore(Map<String, Double> scores) {

		if (scores == null || scores.isEmpty()) {
			return 0.0d;
		}
		double max = scores.values().stream()
				.filter(Objects::nonNull)
				.mapToDouble(Double::doubleValue)
				.max()
				.orElse(0.0d);
		return Math.round(max * 100d) / 100d;
	}

	private ProductModerationDecision resolveDecision(double score) {

		double reviewThreshold = moderationProperties.resolvedReviewThreshold();
		double blockThreshold = moderationProperties.resolvedBlockThreshold();

		if (score >= blockThreshold) {
			return ProductModerationDecision.BLOCKED;
		}
		if (score >= reviewThreshold) {
			return ProductModerationDecision.REVIEW;
		}
		return ProductModerationDecision.CLEAR;
	}

	private List<String> extractReasons(Map<String, Boolean> flags, Map<String, Double> scores) {

		List<String> reasons = new ArrayList<>();
		if (flags != null) {
			for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
				if (Boolean.TRUE.equals(entry.getValue())) {
					reasons.add(entry.getKey());
				}
			}
		}

		if (reasons.isEmpty() && scores != null) {
			scores.entrySet().stream()
					.sorted(Map.Entry.<String, Double>comparingByValue().reversed())
					.limit(1)
					.map(Map.Entry::getKey)
					.forEach(reasons::add);
		}

		return reasons;
	}

	private String buildMessage(List<String> reasons) {

		if (reasons == null || reasons.isEmpty()) {
			return DEFAULT_MESSAGE;
		}
		List<String> messages = reasons.stream()
				.map(reason -> CATEGORY_MESSAGES.getOrDefault(reason, DEFAULT_MESSAGE))
				.distinct()
				.toList();
		return String.join(" ", messages);
	}

	private ProductModerationDecisionResult fallbackResult(String reason) {

		return new ProductModerationDecisionResult(
				ProductModerationDecision.REVIEW,
				moderationProperties.resolvedReviewThreshold(),
				List.of(reason),
				DEFAULT_MESSAGE,
				SOURCE
		);
	}

	private Moderation resolveModeration(ModerationResponse response) {

		if (response == null || response.getResult() == null) {
			return null;
		}
		return response.getResult().getOutput();
	}

	private ModerationResult resolveModerationResult(Moderation moderation) {

		if (moderation == null || moderation.getResults() == null || moderation.getResults().isEmpty()) {
			return null;
		}
		return moderation.getResults().get(0);
	}

	private Map<String, Boolean> toCategoryFlags(Categories categories) {

		if (categories == null) {
			return Map.of();
		}
		Map<String, Boolean> flags = new LinkedHashMap<>();
		flags.put("sexual", categories.isSexual());
		flags.put("sexual/minors", categories.isSexualMinors());
		flags.put("harassment", categories.isHarassment());
		flags.put("harassment/threatening", categories.isHarassmentThreatening());
		flags.put("hate", categories.isHate());
		flags.put("hate/threatening", categories.isHateThreatening());
		flags.put("self-harm", categories.isSelfHarm());
		flags.put("self-harm/intent", categories.isSelfHarmIntent());
		flags.put("self-harm/instructions", categories.isSelfHarmInstructions());
		flags.put("violence", categories.isViolence());
		flags.put("violence/graphic", categories.isViolenceGraphic());
		return flags;
	}

	private Map<String, Double> toCategoryScores(CategoryScores scores) {

		if (scores == null) {
			return Map.of();
		}
		Map<String, Double> categoryScores = new LinkedHashMap<>();
		categoryScores.put("sexual", scores.getSexual());
		categoryScores.put("sexual/minors", scores.getSexualMinors());
		categoryScores.put("harassment", scores.getHarassment());
		categoryScores.put("harassment/threatening", scores.getHarassmentThreatening());
		categoryScores.put("hate", scores.getHate());
		categoryScores.put("hate/threatening", scores.getHateThreatening());
		categoryScores.put("self-harm", scores.getSelfHarm());
		categoryScores.put("self-harm/intent", scores.getSelfHarmIntent());
		categoryScores.put("self-harm/instructions", scores.getSelfHarmInstructions());
		categoryScores.put("violence", scores.getViolence());
		categoryScores.put("violence/graphic", scores.getViolenceGraphic());
		return categoryScores;
	}

	private String buildInput(ProductModerationRequestedEvent event) {

		StringBuilder builder = new StringBuilder();
		appendText(builder, "name", event.name(), moderationProperties.resolvedMaxTextLength());
		appendText(builder, "description", event.description(), moderationProperties.resolvedMaxTextLength());
		appendSpecs(builder, event.specValues(), moderationProperties.resolvedMaxSpecValueLength());
		appendImages(builder, event.imageUrls(), moderationProperties.resolvedMaxImageUrls());
		return builder.toString();
	}

	private void appendText(StringBuilder builder, String label, String value, int limit) {

		if (value == null || value.isBlank()) {
			return;
		}
		builder.append(label).append(": ").append(truncate(value, limit)).append("\n");
	}

	private void appendSpecs(StringBuilder builder, List<String> specs, int limit) {

		if (specs == null || specs.isEmpty()) {
			return;
		}
		List<String> clipped = specs.stream()
				.filter(Objects::nonNull)
				.map(value -> truncate(value, limit))
				.toList();
		builder.append("specs: ").append(String.join(" | ", clipped)).append("\n");
	}

	private void appendImages(StringBuilder builder, List<String> imageUrls, int maxImages) {

		if (imageUrls == null || imageUrls.isEmpty()) {
			return;
		}
		List<String> trimmed = new ArrayList<>();
		for (String url : imageUrls) {
			if (url == null || url.isBlank()) {
				continue;
			}
			trimmed.add(url);
			if (trimmed.size() >= maxImages) {
				break;
			}
		}
		if (!trimmed.isEmpty()) {
			builder.append("image_urls: ").append(String.join(" | ", trimmed));
		}
	}

	private String truncate(String value, int limit) {

		if (value.length() <= limit) {
			return value;
		}
		return value.substring(0, limit);
	}

	private static Map<String, String> buildCategoryMessages() {

		Map<String, String> messages = new LinkedHashMap<>();
		messages.put("sexual", "선정성 표현이 감지되었습니다.");
		messages.put("sexual/minors", "미성년자 관련 선정성 표현이 감지되었습니다.");
		messages.put("harassment", "괴롭힘/혐오 표현이 감지되었습니다.");
		messages.put("harassment/threatening", "협박성 표현이 감지되었습니다.");
		messages.put("hate", "혐오 표현이 감지되었습니다.");
		messages.put("hate/threatening", "혐오/협박 표현이 감지되었습니다.");
		messages.put("self-harm", "자해 관련 표현이 감지되었습니다.");
		messages.put("self-harm/intent", "자해 의도 표현이 감지되었습니다.");
		messages.put("self-harm/instructions", "자해 방법 표현이 감지되었습니다.");
		messages.put("violence", "폭력 표현이 감지되었습니다.");
		messages.put("violence/graphic", "잔혹한 폭력 표현이 감지되었습니다.");
		messages.put("illicit", "불법 거래 가능성이 있는 표현이 감지되었습니다.");
		messages.put("illicit/violent", "불법/폭력 관련 표현이 감지되었습니다.");
		messages.put("political", "정치적 민감 표현이 감지되었습니다.");
		return messages;
	}
}
