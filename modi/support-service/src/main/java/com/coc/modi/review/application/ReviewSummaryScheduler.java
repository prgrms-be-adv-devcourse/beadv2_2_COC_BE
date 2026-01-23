package com.coc.modi.review.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewSummaryScheduler {

	private final ReviewSummaryService reviewSummaryService;

	@Scheduled(cron = "${review.summary.refresh-cron}")
	public void refreshSummaries() {
		reviewSummaryService.refreshAllSummaries();
	}
}
