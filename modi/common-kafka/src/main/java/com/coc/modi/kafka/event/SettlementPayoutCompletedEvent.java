package com.coc.modi.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementPayoutCompletedEvent(
		String eventId,
		Instant occurredAt,
		Long settlementId,
		Long sellerId,
		Long memberId,
		BigDecimal amount
) {

	public static SettlementPayoutCompletedEvent of(
			Long settlementId,
			Long sellerId,
			Long memberId,
			BigDecimal amount
	) {

		return new SettlementPayoutCompletedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				settlementId,
				sellerId,
				memberId,
				amount
		);
	}
}
