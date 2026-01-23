package com.coc.modi.seller.settlement.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.RentalClosedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.seller.settlement.application.SettlementAggregationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RentalClosedEventListener {

	private static final String TYPE_RETURNED = "RETURNED";
	private static final String TYPE_REFUNDED = "REFUNDED";

	private final SettlementAggregationService settlementAggregationService;

	@KafkaListener(
			topics = KafkaTopics.RENTAL_CLOSED_EVENTS,
			groupId = "seller-service",
			containerFactory = "rentalClosedKafkaListenerContainerFactory"
	)
	public void onClosedEvent(RentalClosedEvent event) {

		if (event == null) {
			return;
		}
		LocalDateTime closedAt = event.closedAt();
		if (closedAt == null) {
			log.warn("정산 이벤트 스킵: closedAt 누락. rentalItemId={}", event.rentalItemId());
			return;
		}

		String periodYm = YearMonth.from(closedAt).toString();
		String type = event.type() == null ? "" : event.type().trim().toUpperCase();

		if (TYPE_RETURNED.equals(type)) {
			BigDecimal rentalAmount = event.rentalAmount();
			if (rentalAmount == null) {
				log.warn("정산 누적 스킵: rentalAmount 누락. rentalItemId={}", event.rentalItemId());
				return;
			}
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
			return;
		}

		if (TYPE_REFUNDED.equals(type)) {
			boolean canceled = settlementAggregationService.cancelLine(
					event.sellerId(),
					periodYm,
					event.rentalItemId(),
					null
			);
			if (canceled) {
				log.info("정산 라인 취소 완료. rentalItemId={}, periodYm={}", event.rentalItemId(), periodYm);
			}
			return;
		}

		log.warn("지원하지 않는 정산 이벤트 타입입니다. type={}, rentalItemId={}", event.type(), event.rentalItemId());
	}
}
