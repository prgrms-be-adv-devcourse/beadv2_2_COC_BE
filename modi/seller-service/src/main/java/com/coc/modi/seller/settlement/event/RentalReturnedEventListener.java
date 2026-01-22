package com.coc.modi.seller.settlement.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.RentalReturnedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.seller.settlement.application.SettlementAggregationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RentalReturnedEventListener {

	private final SettlementAggregationService settlementAggregationService;

	@KafkaListener(
			topics = KafkaTopics.RENTAL_RETURNED_EVENTS,
			groupId = "seller-service",
			containerFactory = "rentalReturnedKafkaListenerContainerFactory"
	)
	public void onRentalReturned(RentalReturnedEvent event) {

		if (event == null) {
			return;
		}
		LocalDateTime returnedAt = event.returnedAt();
		if (returnedAt == null) {
			log.warn("정산 누적 스킵: returnedAt 누락. rentalItemId={}", event.rentalItemId());
			return;
		}
		BigDecimal rentalAmount = event.rentalAmount();
		if (rentalAmount == null) {
			log.warn("정산 누적 스킵: rentalAmount 누락. rentalItemId={}", event.rentalItemId());
			return;
		}

		String periodYm = YearMonth.from(returnedAt).toString();
		boolean inserted = settlementAggregationService.aggregateLine(
				null,
				event.sellerId(),
				periodYm,
				event.rentalItemId(),
				event.memberId(),
				event.productId(),
				rentalAmount
		);

		if (inserted) {
			log.info("정산 누적 완료. rentalItemId={}, periodYm={}", event.rentalItemId(), periodYm);
		}
	}
}
