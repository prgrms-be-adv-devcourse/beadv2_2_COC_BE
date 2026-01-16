package com.coc.modi.delivery.delivery.scheduler;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.delivery.delivery.application.DeliveryStatusMapper;
import com.coc.modi.delivery.delivery.domain.Delivery;
import com.coc.modi.delivery.delivery.domain.DeliveryRepository;
import com.coc.modi.delivery.delivery.domain.DeliveryStatus;
import com.coc.modi.delivery.delivery.infrastructure.CarrierTrackingClient;
import com.coc.modi.delivery.delivery.infrastructure.TrackingResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryTrackingService {
	
	private final DeliveryRepository deliveryRepository;
	private final CarrierTrackingClient carrierTrackingClient;
	private final DeliveryStatusMapper deliveryStatusMapper;
	
	@Transactional
	public void claimAndProcess(int limit, int intervalMinutes) {
		
		List<Delivery> targets = deliveryRepository.findTargetsForTrackingWithSkipLocked(intervalMinutes, limit);
		int failCount = 0;
		
		log.info("[배송추적] 대상 건수: {}", targets.size());
		
		for (Delivery d : targets) {
			
			try {
				
				log.info("[배송추적] 처리 시작. deliveryId: {}", d.getId());
				
				TrackingResult result = carrierTrackingClient.track(d.getCarrierCode(), d.getTrackingNumber());
				DeliveryStatus mapped = deliveryStatusMapper.map(result);
				
				d.applyTrackingResult(mapped, result);
				d.markTrackedNow();
				
				log.info("[배송추적] 처리 완료. deliveryId: {}, status: {}", d.getId(), mapped);
				
			} catch (Exception e) {
				
				d.markTrackedNow();
				failCount++;
				log.warn("[배송추적] 처리 실패. deliveryId: {}, carrierCode: {}, trackingNumber: {}",
						d.getId(), d.getCarrierCode(), d.getTrackingNumber(), e);
			}
		}
		
		if (failCount > 0) {
			
			log.warn("[배송추적] 처리 실패 건수: {}", failCount);
		}
	}
}
