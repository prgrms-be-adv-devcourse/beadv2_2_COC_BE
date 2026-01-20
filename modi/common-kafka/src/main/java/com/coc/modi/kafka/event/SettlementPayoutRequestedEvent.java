package com.coc.modi.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementPayoutRequestedEvent(
		String eventId,
		Instant occurredAt,
		Long settlementId,
		Long sellerId,
		Long memberId,
		BigDecimal amount
) {
	
	public static SettlementPayoutRequestedEvent of(
			Long settlementId,
			Long sellerId,
			Long memberId,
			BigDecimal amount
	) {
		
		return new SettlementPayoutRequestedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				settlementId,
				sellerId,
				memberId,
				amount
		);
	}
}
