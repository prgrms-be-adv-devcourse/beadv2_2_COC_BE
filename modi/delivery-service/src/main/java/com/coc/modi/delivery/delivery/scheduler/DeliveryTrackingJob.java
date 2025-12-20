package com.coc.modi.delivery.delivery.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryTrackingJob {
	
	private final DeliveryTrackingService deliveryTrackingService;
	
	@Scheduled(fixedDelayString = "PT5M")
	public void run() {
		
		int limit = 200;
		int intervalMinutes = 10;
		
		log.info("[배송추적] 시작 limit={} intervalMinutes={}", limit, intervalMinutes);
		
		deliveryTrackingService.claimAndProcess(limit, intervalMinutes);
		
		log.info("[배송추적] 종료 limit: {}, intervalMinutes: {}", limit, intervalMinutes);
	}
	
}
