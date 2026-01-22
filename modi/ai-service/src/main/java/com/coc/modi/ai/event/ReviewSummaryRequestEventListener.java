package com.coc.modi.ai.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.coc.modi.ai.review.application.ReviewSummaryGenerator;
import com.coc.modi.kafka.event.ReviewSummaryRequestEvent;
import com.coc.modi.kafka.event.ReviewSummaryResultEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSummaryRequestEventListener {

	private final ReviewSummaryGenerator reviewSummaryGenerator;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@KafkaListener(
			topics = KafkaTopics.REVIEW_SUMMARY_REQUEST_EVENTS,
			groupId = "ai-service",
			containerFactory = "reviewSummaryRequestKafkaListenerContainerFactory"
	)
	public void onReviewSummaryRequest(ReviewSummaryRequestEvent event) {
		ReviewSummaryResultEvent result = reviewSummaryGenerator.generate(event);
		if (result == null) {
			log.warn("Review summary generation skipped. sellerId={} targetType={}",
					event.sellerId(), event.targetType());
			return;
		}

		kafkaTemplate.send(
				KafkaTopics.REVIEW_SUMMARY_RESULT_EVENTS,
				event.sellerId().toString(),
				result
		);
		log.info("Review summary generated. sellerId={} targetType={}", event.sellerId(), event.targetType());
	}
}
