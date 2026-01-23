package com.coc.modi.review.application;

import com.coc.modi.review.application.dto.ReviewSummaryResponse;
import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewRepository;
import com.coc.modi.review.domain.ReviewSummaryBucket;
import com.coc.modi.review.domain.ReviewSummaryBucketRepository;
import com.coc.modi.review.domain.ReviewStatus;
import com.coc.modi.review.domain.ReviewSummary;
import com.coc.modi.review.domain.ReviewSummaryRepository;
import com.coc.modi.kafka.event.ReviewSummaryRequestEvent;
import com.coc.modi.kafka.event.ReviewSummaryResultEvent;
import com.coc.modi.review.outbox.ReviewOutboxService;
import lombok.RequiredArgsConstructor;
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

	private final ReviewRepository reviewRepository;
	private final ReviewSummaryRepository reviewSummaryRepository;
	private final ReviewSummaryBucketRepository reviewSummaryBucketRepository;
	private final ReviewOutboxService reviewOutboxService;
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
		requestFinalSummary(sellerId, totalCount);
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

			requestBucketSummary(sellerId, seedReviews, minTotal);
			return;
		}

		Long lastReviewId = latestBucketOptional.get().getLastReviewId();
		boolean created = false;
		while (true) {
			List<Review> newReviews = fetchNewReviewsAfter(sellerId, lastReviewId, minNew);
			if (newReviews.size() < minNew) {
				break;
			}
			Long newLastReviewId = requestBucketSummary(sellerId, newReviews, minNew);
			if (newLastReviewId == null) {
				break;
			}
			lastReviewId = newLastReviewId;
			created = true;
		}

		if (created
				&& (summaryOptional.isEmpty()
				|| summaryOptional.get().getSummary() == null
				|| summaryOptional.get().getSummary().isBlank())) {
			requestFinalSummary(sellerId, totalCount);
		}
	}

	@Transactional
	public void handleSummaryResult(ReviewSummaryResultEvent event) {
		if (event == null || event.summary() == null || event.summary().isBlank()) {
			return;
		}

		if ("BUCKET".equals(event.targetType())) {
			if (event.lastReviewId() == null) {
				return;
			}

			if (reviewSummaryBucketRepository.findBySellerIdAndLastReviewId(event.sellerId(), event.lastReviewId()).isPresent()) {
				return;
			}

			ReviewSummaryBucket bucket = ReviewSummaryBucket.create(
					event.sellerId(),
					event.reviewCount(),
					event.lastReviewId(),
					event.summary()
			);
			reviewSummaryBucketRepository.save(bucket);

			Optional<ReviewSummary> summaryOptional = reviewSummaryRepository.findBySellerId(event.sellerId());
			if (summaryOptional.isEmpty()
					|| summaryOptional.get().getSummary() == null
					|| summaryOptional.get().getSummary().isBlank()) {
				long totalCount = resolveTotalCount(summaryOptional, event.sellerId());
				requestFinalSummary(event.sellerId(), totalCount);
			}
			return;
		}

		if ("FINAL".equals(event.targetType())) {
			if (event.lastBucketId() == null) {
				return;
			}

			Optional<ReviewSummary> summaryOptional = reviewSummaryRepository.findBySellerId(event.sellerId());
			if (summaryOptional.isPresent()
					&& event.lastBucketId().equals(summaryOptional.get().getLastBucketId())
					&& summaryOptional.get().getSummary() != null
					&& !summaryOptional.get().getSummary().isBlank()) {
				return;
			}

			long ratingSum = resolveRatingSum(summaryOptional, event.sellerId());
			long totalCount = event.totalCount() > 0 ? event.totalCount()
					: resolveTotalCount(summaryOptional, event.sellerId());

			summaryOptional
					.ifPresentOrElse(
							existing -> existing.updateSummary(event.summary(), totalCount, totalCount, ratingSum, event.lastBucketId()),
							() -> reviewSummaryRepository.save(
									ReviewSummary.create(event.sellerId(), totalCount, totalCount, ratingSum, event.lastBucketId(), event.summary())
							)
					);
		}
	}

	private long resolveTotalCount(Optional<ReviewSummary> summaryOptional, Long sellerId) {
		return summaryOptional
				.map(ReviewSummary::getTotalReviewCount)
				.filter(count -> count > 0)
				.orElseGet(() -> reviewRepository.countBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE));
	}

	private void requestFinalSummary(Long sellerId, long totalCount) {

		SummaryPayload payload = generateSummaryPayload(sellerId);
		if (payload == null || payload.payload() == null || payload.payload().isBlank()) {
			return;
		}

		ReviewSummaryRequestEvent event = ReviewSummaryRequestEvent.forFinal(
				sellerId,
				payload.lastBucketId(),
				payload.reviewCount(),
				totalCount,
				policyProperties.getMaxLength(),
				buildPrompt(payload.payload()),
				payload.payload()
		);

		reviewOutboxService.enqueueReviewSummaryRequest(sellerId, event);
	}

	private SummaryPayload generateSummaryPayload(Long sellerId) {

		List<ReviewSummaryBucket> buckets = fetchLatestBucketsForRecentLimit(sellerId);
		if (buckets.isEmpty()) {
			return null;
		}

		Long lastBucketId = buckets.get(buckets.size() - 1).getId();
		List<String> summaries = buckets.stream()
				.map(ReviewSummaryBucket::getSummary)
				.filter(summary -> summary != null && !summary.isBlank())
				.toList();
		if (summaries.isEmpty()) {
			return null;
		}

		String payload = buildPayload(summaries);
		if (payload.isBlank()) {
			return null;
		}

		return new SummaryPayload(payload, lastBucketId, summaries.size());
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

	private Long requestBucketSummary(Long sellerId, List<Review> reviews, int expectedCount) {

		List<Review> ordered = reviews.stream()
				.sorted(Comparator.comparing(Review::getId))
				.toList();

		List<String> contents = ordered.stream()
				.map(Review::getContent)
				.toList();

		String payload = buildPayload(contents);
		if (payload.isBlank()) {
			return null;
		}

		Long lastReviewId = ordered.get(ordered.size() - 1).getId();
		int reviewCount = Math.min(expectedCount, ordered.size());

		ReviewSummaryRequestEvent event = ReviewSummaryRequestEvent.forBucket(
				sellerId,
				lastReviewId,
				reviewCount,
				policyProperties.getMaxLength(),
				buildPrompt(payload),
				payload
		);

		reviewOutboxService.enqueueReviewSummaryRequest(sellerId, event);

		return lastReviewId;
	}

	private record SummaryPayload(String payload, Long lastBucketId, int reviewCount) {
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
}
