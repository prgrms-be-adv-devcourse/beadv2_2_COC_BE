package com.coc.modi.review.application;

import com.coc.modi.review.application.dto.ReviewSummaryResponse;
import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewRepository;
import com.coc.modi.review.domain.ReviewSummaryBucket;
import com.coc.modi.review.domain.ReviewSummaryBucketRepository;
import com.coc.modi.review.domain.ReviewStatus;
import com.coc.modi.review.domain.ReviewSummary;
import com.coc.modi.review.domain.ReviewSummaryRepository;
import com.coc.modi.ai.chat.application.ChatService;
import com.coc.modi.ai.chat.domain.ChatResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

	private static final Logger log = LoggerFactory.getLogger(ReviewSummaryService.class);
	private final ReviewRepository reviewRepository;
	private final ReviewSummaryRepository reviewSummaryRepository;
	private final ReviewSummaryBucketRepository reviewSummaryBucketRepository;
	private final ChatService chatService;
	private final ReviewSummaryPolicyProperties policyProperties;

	@Transactional(readOnly = true)
	public Optional<ReviewSummaryResponse> getSummary(Long sellerId) {

		return reviewSummaryRepository.findBySellerId(sellerId)
				.filter(summary -> summary.getSummary() != null && !summary.getSummary().isBlank())
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

		Optional<ReviewSummaryBucket> latestBucketOptional = reviewSummaryBucketRepository.findLatestBySellerId(sellerId);
		if (latestBucketOptional.isEmpty()) {
			return;
		}

		Optional<ReviewSummary> summaryOptional = reviewSummaryRepository.findBySellerId(sellerId);
		Long latestBucketId = latestBucketOptional.get().getId();
		if (summaryOptional.isPresent()
				&& latestBucketId.equals(summaryOptional.get().getLastBucketId())
				&& summaryOptional.get().getSummary() != null
				&& !summaryOptional.get().getSummary().isBlank()) {
			return;
		}

		long totalCount = resolveTotalCount(summaryOptional, sellerId);
		updateFinalSummary(sellerId, totalCount);
	}

	@Transactional
	public void handleReviewCreated(Long sellerId) {

		int minTotal = policyProperties.getMinTotalCount();
		int minNew = policyProperties.getMinNewCount();

		Optional<ReviewSummary> summaryOptional = reviewSummaryRepository.findBySellerId(sellerId);
		long totalCount = resolveTotalCount(summaryOptional, sellerId);

		Optional<ReviewSummaryBucket> latestBucketOptional = reviewSummaryBucketRepository.findLatestBySellerId(sellerId);
		if (latestBucketOptional.isEmpty()) {
			if (totalCount < minTotal) {
				return;
			}

			List<Review> seedReviews = fetchLatestReviews(sellerId, minTotal);
			if (seedReviews.size() < minTotal) {
				return;
			}

			ReviewSummaryBucket bucket = buildBucketFromReviews(sellerId, seedReviews, minTotal);
			reviewSummaryBucketRepository.save(bucket);

			if (summaryOptional.isEmpty()
					|| summaryOptional.get().getSummary() == null
					|| summaryOptional.get().getSummary().isBlank()) {
				updateFinalSummary(sellerId, totalCount);
			}
			return;
		}

		Long lastReviewId = latestBucketOptional.get().getLastReviewId();
		boolean created = false;
		while (true) {
			List<Review> newReviews = fetchNewReviewsAfter(sellerId, lastReviewId, minNew);
			if (newReviews.size() < minNew) {
				break;
			}
			ReviewSummaryBucket bucket = buildBucketFromReviews(sellerId, newReviews, minNew);
			reviewSummaryBucketRepository.save(bucket);
			lastReviewId = bucket.getLastReviewId();
			created = true;
		}

		if (created
				&& (summaryOptional.isEmpty()
				|| summaryOptional.get().getSummary() == null
				|| summaryOptional.get().getSummary().isBlank())) {
			updateFinalSummary(sellerId, totalCount);
		}
	}

	private long resolveTotalCount(Optional<ReviewSummary> summaryOptional, Long sellerId) {
		return summaryOptional
				.map(ReviewSummary::getTotalReviewCount)
				.filter(count -> count > 0)
				.orElseGet(() -> reviewRepository.countBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE));
	}

	private void updateFinalSummary(Long sellerId, long totalCount) {

		SummaryPayload payload = generateSummaryPayload(sellerId);
		if (payload == null || payload.summary() == null || payload.summary().isBlank()) {
			return;
		}

		Optional<ReviewSummary> summaryOptional = reviewSummaryRepository.findBySellerId(sellerId);
		long ratingSum = resolveRatingSum(summaryOptional, sellerId);

		summaryOptional
				.ifPresentOrElse(
						existing -> existing.updateSummary(payload.summary(), totalCount, totalCount, ratingSum, payload.lastBucketId()),
						() -> reviewSummaryRepository.save(
								ReviewSummary.create(sellerId, totalCount, totalCount, ratingSum, payload.lastBucketId(), payload.summary())
						)
				);
	}

	private SummaryPayload generateSummaryPayload(Long sellerId) {

		List<ReviewSummaryBucket> buckets = fetchLatestBucketsForRecentLimit(sellerId);
		if (buckets.isEmpty()) {
			return null;
		}

		Long lastBucketId = buckets.get(buckets.size() - 1).getId();
		if (buckets.size() == 1) {
			return new SummaryPayload(buckets.get(0).getSummary(), lastBucketId);
		}

		List<String> summaries = buckets.stream()
				.map(ReviewSummaryBucket::getSummary)
				.toList();

		String summary = summarizeSellerReviews(summaries);
		if (summary == null || summary.isBlank()) {
			return null;
		}

		return new SummaryPayload(summary, lastBucketId);
	}

	private List<ReviewSummaryBucket> fetchLatestBucketsForRecentLimit(Long sellerId) {

		int minNew = policyProperties.getMinNewCount();
		int recentLimit = policyProperties.getRecentLimit();
		int bucketLimit = (recentLimit + minNew - 1) / minNew + 1;

		List<ReviewSummaryBucket> latestBuckets = reviewSummaryBucketRepository.findLatestBySellerId(
				sellerId,
				PageRequest.of(0, bucketLimit, Sort.by(Sort.Direction.DESC, "lastReviewId"))
		);

		if (latestBuckets.isEmpty()) {
			return List.of();
		}

		int total = 0;
		List<ReviewSummaryBucket> selected = new ArrayList<>();
		for (ReviewSummaryBucket bucket : latestBuckets) {
			selected.add(bucket);
			total += bucket.getReviewCount();
			if (total >= recentLimit) {
				break;
			}
		}

		Collections.reverse(selected);
		return selected;
	}

	private long resolveRatingSum(Optional<ReviewSummary> summaryOptional, Long sellerId) {
		
		return summaryOptional.map(ReviewSummary::getRatingSum)
				.orElseGet(() -> reviewRepository.sumRatingBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE));
	}

	private List<Review> fetchLatestReviews(Long sellerId, int limit) {

		PageRequest pageRequest = PageRequest.of(
				0,
				limit,
				Sort.by(Sort.Direction.DESC, "id")
		);

		return reviewRepository.findBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE, pageRequest)
				.getContent();
	}

	private List<Review> fetchNewReviewsAfter(Long sellerId, Long lastReviewId, int limit) {

		PageRequest pageRequest = PageRequest.of(
				0,
				limit,
				Sort.by(Sort.Direction.ASC, "id")
		);

		return reviewRepository.findBySellerIdAndStatusAndIdGreaterThan(
						sellerId,
						ReviewStatus.ACTIVE,
						lastReviewId,
						pageRequest
				)
				.getContent();
	}

	private ReviewSummaryBucket buildBucketFromReviews(Long sellerId, List<Review> reviews, int expectedCount) {

		List<Review> ordered = reviews.stream()
				.sorted(Comparator.comparing(Review::getId))
				.toList();

		List<String> contents = ordered.stream()
				.map(Review::getContent)
				.toList();

		String summary = summarizeSellerReviews(contents);
		if (summary == null || summary.isBlank()) {
			throw new IllegalStateException("Failed to summarize review bucket");
		}

		Long lastReviewId = ordered.get(ordered.size() - 1).getId();
		int reviewCount = Math.min(expectedCount, ordered.size());

		return ReviewSummaryBucket.create(sellerId, reviewCount, lastReviewId, summary);
	}

	private record SummaryPayload(String summary, Long lastBucketId) {
	}

	private String summarizeSellerReviews(List<String> contents) {

		if (contents == null || contents.isEmpty()) {
			return null;
		}

		String payload = buildPayload(contents);
		if (payload.isBlank()) {
			return null;
		}

		try {
			ChatResult result = chatService.chat(buildPrompt(payload));
			if (result == null || isFallback(result) || result.content() == null || result.content().isBlank()) {
				return fallback(payload);
			}

			return normalize(result.content());

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
		int maxLength = policyProperties.getMaxLength();
		if (trimmed.length() <= maxLength) {

			return trimmed;
		}

		return trimmed.substring(0, maxLength).trim();
	}

	private String fallback(String content) {

		String trimmed = content.replace("\n", " ").trim();
		int maxLength = policyProperties.getMaxLength();
		if (trimmed.length() <= maxLength) {

			return trimmed;
		}

		return trimmed.substring(0, maxLength).trim();
	}

	private String buildPrompt(String payload) {
		return """
				Summarize overall feedback about a seller based on multiple customer reviews.
				- Use up to 6 sentences and keep the total length within %d characters.
				- Include common praise, recurring concerns, and an overall assessment.
				- Avoid speculation and keep a neutral tone.
				Reviews:
				%s
				""".formatted(policyProperties.getMaxLength(), payload);
	}

	private boolean isFallback(ChatResult result) {
		if (result.metadata() == null) {
			return false;
		}
		Object source = result.metadata().get("source");
		return "fallback".equals(source);
	}
}
