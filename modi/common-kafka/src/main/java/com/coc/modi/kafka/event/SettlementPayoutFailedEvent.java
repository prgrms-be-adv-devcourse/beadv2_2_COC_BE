package com.coc.modi.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementPayoutFailedEvent(
		String eventId,
		Instant occurredAt,
		Long settlementId,
		Long sellerId,
		Long memberId,
		BigDecimal amount,
		String failureReason
) {

	public static SettlementPayoutFailedEvent of(
			Long settlementId,
			Long sellerId,
			Long memberId,
			BigDecimal amount,
			String failureReason
	) {

		return new SettlementPayoutFailedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				settlementId,
				sellerId,
				memberId,
				amount,
				failureReason
		);
	}
}
