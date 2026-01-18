package com.coc.modi.seller.settlement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import com.coc.modi.seller.settlement.batch.support.SettlementPayoutFixture;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.exception.SellerSettlementNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SellerSettlementServiceTest {

	@Mock
	private SellerSettlementRepository sellerSettlementRepository;

	@Mock
	private SettlementNotificationService settlementNotificationService;

	@Mock
	private SettlementPayoutRequestPublisher settlementPayoutRequestPublisher;

	@InjectMocks
	private SellerSettlementService sellerSettlementService;

	@Test
	void requestPayoutByAdmin_marksPaidWhenAmountNonPositive() {
		SettlementBatch batch = SettlementPayoutFixture.newBatch("2025-01");
		SellerSettlement settlement = SettlementPayoutFixture.newSettlement(
				batch,
				10L,
				"2025-01",
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				SellerSettlementStatus.READY
		);
		ReflectionTestUtils.setField(settlement, "id", 1L);
		when(sellerSettlementRepository.findById(1L))
				.thenReturn(Optional.of(settlement));

		LocalDateTime paidAt = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
		sellerSettlementService.requestPayoutByAdmin(1L, paidAt);

		assertThat(settlement.getStatus()).isEqualTo(SellerSettlementStatus.PAID);
		assertThat(settlement.getPaidAt()).isEqualTo(paidAt);
		verify(settlementNotificationService).notifySettlementPaid(settlement);
		verify(sellerSettlementRepository, never()).save(settlement);
		verify(settlementPayoutRequestPublisher, never()).publish(settlement);
	}

	@Test
	void requestPayoutByAdmin_requestsPayoutWhenAmountPositive() {
		SettlementBatch batch = SettlementPayoutFixture.newBatch("2025-02");
		SellerSettlement settlement = SettlementPayoutFixture.newSettlement(
				batch,
				11L,
				"2025-02",
				new BigDecimal("10000"),
				new BigDecimal("1000"),
				SellerSettlementStatus.READY
		);
		ReflectionTestUtils.setField(settlement, "id", 2L);
		when(sellerSettlementRepository.findById(2L))
				.thenReturn(Optional.of(settlement));

		sellerSettlementService.requestPayoutByAdmin(2L, LocalDateTime.now());

		assertThat(settlement.getStatus()).isEqualTo(SellerSettlementStatus.PENDING);
		verify(sellerSettlementRepository).save(settlement);
		verify(settlementPayoutRequestPublisher).publish(settlement);
		verify(settlementNotificationService, never()).notifySettlementPaid(settlement);
	}

	@Test
	void requestPayoutByAdmin_throwsWhenSettlementMissing() {
		when(sellerSettlementRepository.findById(3L))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> sellerSettlementService.requestPayoutByAdmin(3L, LocalDateTime.now()))
				.isInstanceOf(SellerSettlementNotFoundException.class);

		verifyNoInteractions(settlementNotificationService, settlementPayoutRequestPublisher);
	}
}
