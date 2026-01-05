package com.coc.modi.seller.settlement.application;

import com.coc.modi.common.NotificationType;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementNotificationService {

	private final SellerRepository sellerRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void notifySettlementPaid(SellerSettlement settlement) {

		if (settlement == null) {
			return;
		}

		Long sellerId = settlement.getSellerId();
		Long settlementId = settlement.getId();
		if (sellerId == null || settlementId == null) {
			return;
		}

		Seller seller = sellerRepository.findById(sellerId).orElse(null);
		if (seller == null || seller.getMemberId() == null) {
			log.warn("정산 알림 발행 실패: 판매자 정보를 찾을 수 없습니다. sellerId={} settlementId={}",
					sellerId, settlementId);
			return;
		}

		NotificationEvent event = NotificationEvent.of(
				seller.getMemberId(),
				NotificationType.SETTLEMENT_PAID.name(),
				"정산이 완료되었습니다",
				"정산금이 지급되었습니다.",
				"SETTLEMENT",
				settlementId.toString()
		);

		try {
			kafkaTemplate.send(KafkaTopics.NOTIFICATION_EVENTS, seller.getMemberId().toString(), event);
		} catch (Exception ex) {
			log.warn("정산 알림 발행 실패. settlementId={} sellerId={} memberId={}",
					settlementId, sellerId, seller.getMemberId(), ex);
		}
	}
}
