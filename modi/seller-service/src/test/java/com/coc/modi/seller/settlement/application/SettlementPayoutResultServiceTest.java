package com.coc.modi.seller.settlement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import com.coc.modi.kafka.event.SettlementPayoutCompletedEvent;
import com.coc.modi.kafka.event.SettlementPayoutFailedEvent;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutFixture;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SettlementPayoutResultServiceTest {

	@Mock
	private SellerSettlementRepository sellerSettlementRepository;

	@Mock
	private SettlementNotificationService settlementNotificationService;

	@InjectMocks
	private SettlementPayoutResultService settlementPayoutResultService;

	@Test
	void handleCompleted_marksPaidAndNotifies() {
		SettlementBatch batch = SettlementPayoutFixture.newBatch("2025-01");
		SellerSettlement settlement = SettlementPayoutFixture.newSettlement(
				batch,
				10L,
				"2025-01",
				new BigDecimal("10000"),
				new BigDecimal("1000"),
				SellerSettlementStatus.READY
		);
		ReflectionTestUtils.setField(settlement, "id", 1L);
		settlement.requestPayout();

		when(sellerSettlementRepository.findById(settlement.getId()))
				.thenReturn(Optional.of(settlement));

		SettlementPayoutCompletedEvent event = SettlementPayoutCompletedEvent.of(
				1L,
				10L,
				100L,
				new BigDecimal("9000")
		);

		settlementPayoutResultService.handleCompleted(event);

		assertThat(settlement.getStatus()).isEqualTo(SellerSettlementStatus.PAID);
		verify(sellerSettlementRepository).save(settlement);
		verify(settlementNotificationService).notifySettlementPaid(settlement);
	}

	@Test
	void handleFailed_marksFailedWithReason() {
		SettlementBatch batch = SettlementPayoutFixture.newBatch("2025-02");
		SellerSettlement settlement = SettlementPayoutFixture.newSettlement(
				batch,
				11L,
				"2025-02",
				new BigDecimal("5000"),
				new BigDecimal("500"),
				SellerSettlementStatus.READY
		);
		ReflectionTestUtils.setField(settlement, "id", 2L);
		settlement.requestPayout();

		when(sellerSettlementRepository.findById(settlement.getId()))
				.thenReturn(Optional.of(settlement));

		SettlementPayoutFailedEvent event = SettlementPayoutFailedEvent.of(
				2L,
				11L,
				101L,
				new BigDecimal("4500"),
				"지급 실패"
		);

		settlementPayoutResultService.handleFailed(event);

		assertThat(settlement.getStatus()).isEqualTo(SellerSettlementStatus.FAILED);
		assertThat(settlement.getFailureReason()).isEqualTo("지급 실패");
		verify(sellerSettlementRepository).save(settlement);
		verify(settlementNotificationService, never()).notifySettlementPaid(settlement);
	}
}
