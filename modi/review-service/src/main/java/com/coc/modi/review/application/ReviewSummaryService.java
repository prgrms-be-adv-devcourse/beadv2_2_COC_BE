package com.coc.modi.review.application;

import com.coc.modi.review.application.dto.ReviewSummaryResponse;
import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewRepository;
import com.coc.modi.review.domain.ReviewStatus;
import com.coc.modi.review.domain.ReviewSummary;
import com.coc.modi.review.domain.ReviewSummaryRepository;
import com.coc.modi.review.infrastructure.openai.OpenAiClient;
import com.coc.modi.review.infrastructure.openai.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

	private static final Logger log = LoggerFactory.getLogger(ReviewSummaryService.class);
	private final ReviewRepository reviewRepository;
	private final ReviewSummaryRepository reviewSummaryRepository;
	private final OpenAiClient openAiClient;
	private final OpenAiProperties openAiProperties;
	private final ReviewSummaryPolicyProperties policyProperties;

	@Transactional(readOnly = true)
	public Optional<ReviewSummaryResponse> getSummary(Long sellerId) {

		long totalCount = reviewRepository.countBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE);
		if (totalCount < policyProperties.getMinTotalCount()) {
			return Optional.empty();
		}

		return reviewSummaryRepository.findBySellerId(sellerId)
				.map(ReviewSummaryResponse::from);
	}

	@Transactional
	public void refreshAllSummaries() {

		List<Long> sellerIds = reviewRepository.findDistinctSellerIdsByStatus(ReviewStatus.ACTIVE);
		for (Long sellerId : sellerIds) {
			refreshSummaryIfNeeded(sellerId);
		}
	}

	@Transactional
	public void refreshSummaryIfNeeded(Long sellerId) {

		long totalCount = reviewRepository.countBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE);
		Optional<ReviewSummary> existingOptional = reviewSummaryRepository.findBySellerId(sellerId);

		if (totalCount < policyProperties.getMinTotalCount()) {
			existingOptional.ifPresent(reviewSummaryRepository::delete);
			return;
		}

		if (existingOptional.isEmpty()) {
			createSummary(sellerId, totalCount);
			return;
		}

		ReviewSummary existing = existingOptional.get();
		long delta = totalCount - existing.getReviewCount();

		if (delta >= policyProperties.getMinNewCount() || delta < 0) {
			updateSummary(existing, totalCount);
		}
	}

	private void createSummary(Long sellerId, long totalCount) {

		String summary = generateSummary(sellerId);
		if (summary == null || summary.isBlank()) {
			return;
		}

		reviewSummaryRepository.save(ReviewSummary.create(sellerId, totalCount, summary));
	}

	private void updateSummary(ReviewSummary existing, long totalCount) {

		String summary = generateSummary(existing.getSellerId());
		if (summary == null || summary.isBlank()) {
			return;
		}

		existing.updateSummary(summary, totalCount);
	}

	private String generateSummary(Long sellerId) {

		PageRequest pageRequest = PageRequest.of(
				0,
				policyProperties.getRecentLimit(),
				Sort.by(Sort.Direction.DESC, "createdAt")
		);

		List<Review> reviews = reviewRepository.findBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE, pageRequest)
				.getContent();

		List<String> contents = reviews.stream()
				.map(Review::getContent)
				.toList();

		return summarizeSellerReviews(contents);
	}

	private String summarizeSellerReviews(List<String> contents) {

		if (contents == null || contents.isEmpty()) {
			return null;
		}

		String payload = buildPayload(contents);
		if (payload.isBlank()) {
			return null;
		}

		if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
			return fallback(payload);
		}

		try {
			String summary = openAiClient.summarizeSellerReviews(payload);
			if (summary == null || summary.isBlank()) {
				return fallback(payload);
			}

			return normalize(summary);

		} catch (Exception ex) {

			log.warn("Failed to summarize seller reviews with OpenAI", ex);

			return fallback(payload);
		}
	}

	private String buildPayload(List<String> contents) {

		List<String> sanitized = contents.stream()
				.filter(content -> content != null && !content.isBlank())
				.map(content -> content.replace("\n", " ").trim())
				.filter(content -> !content.isBlank())
				.toList();

		if (sanitized.isEmpty()) {
			return "";
		}

		return sanitized.stream()
				.limit(100)
				.map(content -> "- " + content)
				.collect(Collectors.joining("\n"));
	}

	private String normalize(String summary) {

		String trimmed = summary.replace("\n", " ").trim();
		int maxLength = openAiProperties.getSummary().getMaxLength();
		if (trimmed.length() <= maxLength) {

			return trimmed;
		}

		return trimmed.substring(0, maxLength).trim();
	}

	private String fallback(String content) {

		String trimmed = content.replace("\n", " ").trim();
		int maxLength = openAiProperties.getSummary().getMaxLength();
		if (trimmed.length() <= maxLength) {

			return trimmed;
		}

		return trimmed.substring(0, maxLength).trim();
	}
}
