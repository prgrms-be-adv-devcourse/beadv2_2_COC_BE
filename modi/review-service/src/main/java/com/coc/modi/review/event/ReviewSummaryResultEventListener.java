package com.coc.modi.review.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.kafka.event.ReviewSummaryResultEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.review.application.ReviewSummaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSummaryResultEventListener {

	private final ReviewSummaryService reviewSummaryService;

	@KafkaListener(
			topics = KafkaTopics.REVIEW_SUMMARY_RESULT_EVENTS,
			groupId = "review-service",
			containerFactory = "reviewSummaryResultKafkaListenerContainerFactory"
	)
	public void onReviewSummaryResult(ReviewSummaryResultEvent event) {
		reviewSummaryService.handleSummaryResult(event);
		log.info("Review summary result processed. sellerId={}, targetType={}",
				event.sellerId(), event.targetType());
	}
}
