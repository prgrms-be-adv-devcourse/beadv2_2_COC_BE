package com.coc.modi.seller.settlement.application;

import com.coc.modi.kafka.event.SettlementPayoutRequestedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementPayoutRequestPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final SellerRepository sellerRepository;

	public void publish(SellerSettlement settlement) {

		if (settlement == null) {
			return;
		}

		Long settlementId = settlement.getId();
		Long sellerId = settlement.getSellerId();
		BigDecimal amount = settlement.getSettlementAmount();
		if (settlementId == null || sellerId == null) {
			return;
		}

		Long memberId = sellerRepository.findById(sellerId)
				.map(Seller::getMemberId)
				.orElse(null);
		if (memberId == null) {
			log.warn("정산 지급 요청 발행 실패: 판매자 정보를 찾을 수 없습니다. sellerId={} settlementId={}",
					sellerId, settlementId);
			return;
		}

		publish(settlementId, sellerId, memberId, amount);
	}

	public void publish(Long settlementId, Long sellerId, Long memberId, BigDecimal amount) {

		if (settlementId == null || sellerId == null || memberId == null) {
			return;
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}

		SettlementPayoutRequestedEvent event = SettlementPayoutRequestedEvent.of(
				settlementId,
				sellerId,
				memberId,
				amount
		);

		try {
			kafkaTemplate.send(KafkaTopics.SETTLEMENT_PAYOUT_EVENTS, settlementId.toString(), event);
		} catch (Exception ex) {
			log.warn("정산 지급 요청 발행 실패. settlementId={} sellerId={} memberId={}",
					settlementId, sellerId, memberId, ex);
		}
	}
}
