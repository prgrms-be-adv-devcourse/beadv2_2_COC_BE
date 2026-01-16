package com.coc.modi.account.wallet.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;

import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.exception.AccountException;
import com.coc.modi.common.ErrorCode;
import com.coc.modi.kafka.event.SettlementPayoutRequestedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettlementPayoutEventListenerTest {

	@Mock
	private WalletCommandService walletCommandService;

	@Mock
	private SettlementPayoutEventPublisher settlementPayoutEventPublisher;

	@InjectMocks
	private SettlementPayoutEventListener settlementPayoutEventListener;

	@Test
	void onSettlementPayoutRequested_publishesCompleted() {
		SettlementPayoutRequestedEvent event = SettlementPayoutRequestedEvent.of(
				10L, 20L, 30L, new BigDecimal("1000.00")
		);
		when(walletCommandService.payoutSettlement(30L, 10L, new BigDecimal("1000.00")))
				.thenReturn(true);

		settlementPayoutEventListener.onSettlementPayoutRequested(event);

		verify(settlementPayoutEventPublisher).publishCompleted(10L, 20L, 30L, new BigDecimal("1000.00"));
		verify(settlementPayoutEventPublisher, never())
				.publishFailed(anyLong(), anyLong(), anyLong(), any(BigDecimal.class), anyString());
	}

	@Test
	void onSettlementPayoutRequested_publishesCompletedWhenDuplicate() {
		SettlementPayoutRequestedEvent event = SettlementPayoutRequestedEvent.of(
				11L, 21L, 31L, new BigDecimal("500.00")
		);
		when(walletCommandService.payoutSettlement(31L, 11L, new BigDecimal("500.00")))
				.thenReturn(false);

		settlementPayoutEventListener.onSettlementPayoutRequested(event);

		verify(settlementPayoutEventPublisher).publishCompleted(11L, 21L, 31L, new BigDecimal("500.00"));
		verify(settlementPayoutEventPublisher, never())
				.publishFailed(anyLong(), anyLong(), anyLong(), any(BigDecimal.class), anyString());
	}

	@Test
	void onSettlementPayoutRequested_publishesFailedOnAccountException() {
		SettlementPayoutRequestedEvent event = SettlementPayoutRequestedEvent.of(
				12L, 22L, 32L, new BigDecimal("700.00")
		);
		when(walletCommandService.payoutSettlement(32L, 12L, new BigDecimal("700.00")))
				.thenThrow(new AccountException(ErrorCode.INVALID_INPUT, "bad request"));

		settlementPayoutEventListener.onSettlementPayoutRequested(event);

		verify(settlementPayoutEventPublisher).publishFailed(12L, 22L, 32L, new BigDecimal("700.00"), "bad request");
		verify(settlementPayoutEventPublisher, never()).publishCompleted(12L, 22L, 32L, new BigDecimal("700.00"));
	}

	@Test
	void onSettlementPayoutRequested_ignoresNullEvent() {
		settlementPayoutEventListener.onSettlementPayoutRequested(null);

		verifyNoInteractions(walletCommandService, settlementPayoutEventPublisher);
	}

	@Test
	void onSettlementPayoutRequested_ignoresMissingFields() {
		SettlementPayoutRequestedEvent event = new SettlementPayoutRequestedEvent(
				"event-id",
				Instant.now(),
				null,
				20L,
				30L,
				new BigDecimal("1000.00")
		);

		settlementPayoutEventListener.onSettlementPayoutRequested(event);

		verifyNoInteractions(walletCommandService, settlementPayoutEventPublisher);
	}
}
