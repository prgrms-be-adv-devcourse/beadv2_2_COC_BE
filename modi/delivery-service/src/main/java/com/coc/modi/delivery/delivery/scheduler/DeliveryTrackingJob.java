package com.coc.modi.delivery.delivery.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryTrackingJob {
	
	@Value("${delivery.tracking.limit:200}")
	private int limit;
	
	@Value("${delivery.tracking.intervalMinutes:10}")
	private int intervalMinutes;
	
	private final DeliveryTrackingService deliveryTrackingService;
	
	@Scheduled(fixedDelayString = "${delivery.tracking.fixed-delay:PT5M}")
	@SchedulerLock(
			name = "deliveryTrackingJob",
			lockAtMostFor = "${delivery.tracking.lock-at-most-for:PT5M}",
			lockAtLeastFor = "${delivery.tracking.lock-at-least-for:PT30S}"
	)
	public void run() {
		
		log.info("[배송추적] 시작 limit={} intervalMinutes={}", limit, intervalMinutes);
		
		deliveryTrackingService.claimAndProcess(limit, intervalMinutes);
		
		log.info("[배송추적] 종료 limit: {}, intervalMinutes: {}", limit, intervalMinutes);
	}
	
}
