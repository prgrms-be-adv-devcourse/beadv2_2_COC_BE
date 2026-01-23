package com.coc.modi.ai.review.application;

import com.coc.modi.ai.chat.application.ChatService;
import com.coc.modi.ai.chat.domain.ChatResult;
import com.coc.modi.kafka.event.ReviewSummaryRequestEvent;
import com.coc.modi.kafka.event.ReviewSummaryResultEvent;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewSummaryGenerator {

	private static final Logger log = LoggerFactory.getLogger(ReviewSummaryGenerator.class);

	private final ChatService chatService;

	public ReviewSummaryResultEvent generate(ReviewSummaryRequestEvent event) {
		if (event == null) {
			return null;
		}

		String summary = null;
		try {
			ChatResult result = chatService.chat(event.prompt());
			if (result != null && result.content() != null && !result.content().isBlank()) {
				summary = normalize(result.content(), event.maxLength());
			}
		} catch (Exception ex) {
			log.warn("Review summary generation failed. sellerId={} targetType={}",
					event.sellerId(), event.targetType(), ex);
		}

		if (summary == null || summary.isBlank()) {
			summary = fallback(event.payload(), event.maxLength());
		}

		if (summary == null || summary.isBlank()) {
			return null;
		}

		if ("FINAL".equals(event.targetType())) {
			return ReviewSummaryResultEvent.forFinal(
					event.sellerId(),
					event.lastBucketId(),
					event.reviewCount(),
					event.totalCount(),
					summary
			);
		}

		return ReviewSummaryResultEvent.forBucket(
				event.sellerId(),
				event.lastReviewId(),
				event.reviewCount(),
				summary
		);
	}

	private String normalize(String summary, int maxLength) {
		String trimmed = summary.replace("\n", " ").trim();
		if (trimmed.length() <= maxLength) {
			return trimmed;
		}
		return trimmed.substring(0, maxLength).trim();
	}

	private String fallback(String content, int maxLength) {
		if (content == null || content.isBlank()) {
			return null;
		}
		String trimmed = content.replace("\n", " ").trim();
		if (trimmed.length() <= maxLength) {
			return trimmed;
		}
		return trimmed.substring(0, maxLength).trim();
	}
}
