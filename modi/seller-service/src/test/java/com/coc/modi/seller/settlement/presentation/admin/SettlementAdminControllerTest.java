package com.coc.modi.seller.settlement.presentation.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.SettlementBatchRunner;
import com.coc.modi.seller.settlement.application.SettlementBatchService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchCreateCommand;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchRunCommand;
import com.coc.modi.seller.settlement.application.dto.SettlementBulkPayResponse;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutFixture;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchStatus;
import com.coc.modi.seller.settlement.exception.SettlementAccessDeniedException;
import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;
import com.coc.modi.seller.settlement.presentation.admin.dto.SettlementAdminBatchRunRequest;
import com.coc.modi.seller.settlement.presentation.admin.dto.SettlementBulkPayRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SettlementAdminControllerTest {

	@Mock
	private SellerSettlementService sellerSettlementService;

	@Mock
	private SettlementBatchService settlementBatchService;

	@Mock
	private SettlementBatchRunner settlementBatchRunner;

	@InjectMocks
	private SettlementAdminController settlementAdminController;

	@Test
	void getSettlements_requiresAdmin() {
		Pageable pageable = PageRequest.of(0, 10);

		assertThatThrownBy(() -> settlementAdminController.getSettlements(
				sellerMember(),
				"2025-01",
				1L,
				SellerSettlementStatus.READY,
				pageable
		)).isInstanceOf(SettlementAccessDeniedException.class);
	}

	@Test
	void getSettlements_returnsPageForAdmin() {
		Pageable pageable = PageRequest.of(0, 10);
		SellerSettlementResponse response = responseFixture(1L, "2025-01");
		Page<SellerSettlementResponse> page = new PageImpl<>(List.of(response), pageable, 1);
		when(sellerSettlementService.getSettlementsForAdmin(2L, "2025-01", SellerSettlementStatus.READY, pageable))
				.thenReturn(page);

		ResponseEntity<ApiResponse<Page<SellerSettlementResponse>>> result = settlementAdminController.getSettlements(
				adminMember(),
				"2025-01",
				2L,
				SellerSettlementStatus.READY,
				pageable
		);

		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().getTotalElements()).isEqualTo(1);
		verify(sellerSettlementService).getSettlementsForAdmin(2L, "2025-01", SellerSettlementStatus.READY, pageable);
	}

	@Test
	void paySettlement_requiresAdmin() {
		assertThatThrownBy(() -> settlementAdminController.paySettlement(
				sellerMember(),
				1L,
				null
		)).isInstanceOf(SettlementAccessDeniedException.class);
	}

	@Test
	void paySettlement_rejectsInvalidPaidAt() {
		assertThatThrownBy(() -> settlementAdminController.paySettlement(
				adminMember(),
				1L,
				"invalid"
		)).isInstanceOf(SettlementInputInvalidException.class);
	}

	@Test
	void paySettlement_callsServiceForAdmin() {
		LocalDateTime paidAt = LocalDateTime.of(2025, 2, 3, 4, 5, 6);
		SellerSettlementResponse response = responseFixture(2L, "2025-02");
		when(sellerSettlementService.requestPayoutByAdmin(2L, paidAt)).thenReturn(response);

		ResponseEntity<ApiResponse<SellerSettlementResponse>> result =
				settlementAdminController.paySettlement(adminMember(), 2L, paidAt.toString());

		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().id()).isEqualTo(2L);
		verify(sellerSettlementService).requestPayoutByAdmin(2L, paidAt);
	}

	@Test
	void payBulk_requiresAdmin() {
		SettlementBulkPayRequest request = new SettlementBulkPayRequest("2025-03", 3L, null, null);

		assertThatThrownBy(() -> settlementAdminController.payBulk(sellerMember(), request))
				.isInstanceOf(SettlementAccessDeniedException.class);
	}

	@Test
	void payBulk_rejectsNullRequest() {
		assertThatThrownBy(() -> settlementAdminController.payBulk(adminMember(), null))
				.isInstanceOf(SettlementInputInvalidException.class);
	}

	@Test
	void payBulk_defaultsStatusToFailed() {
		LocalDateTime paidAt = LocalDateTime.of(2025, 3, 1, 10, 0, 0);
		SettlementBulkPayRequest request = new SettlementBulkPayRequest("2025-03", 3L, null, paidAt.toString());
		SettlementBulkPayResponse response = new SettlementBulkPayResponse(2, 1, 1);
		when(sellerSettlementService.requestPayoutsByAdmin(3L, "2025-03", SellerSettlementStatus.FAILED, paidAt))
				.thenReturn(response);

		ResponseEntity<ApiResponse<SettlementBulkPayResponse>> result =
				settlementAdminController.payBulk(adminMember(), request);

		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().requested()).isEqualTo(1);
		verify(sellerSettlementService).requestPayoutsByAdmin(3L, "2025-03", SellerSettlementStatus.FAILED, paidAt);
	}

	@Test
	void runBatch_requiresAdmin() {
		SettlementAdminBatchRunRequest request = new SettlementAdminBatchRunRequest("2025-04", null, null, null, null);

		assertThatThrownBy(() -> settlementAdminController.runBatch(sellerMember(), request))
				.isInstanceOf(SettlementAccessDeniedException.class);
	}

	@Test
	void runBatch_requiresPeriodYm() {
		SettlementAdminBatchRunRequest request = new SettlementAdminBatchRunRequest(null, null, null, null, null);

		assertThatThrownBy(() -> settlementAdminController.runBatch(adminMember(), request))
				.isInstanceOf(SettlementInputInvalidException.class);
	}

	@Test
	void runBatch_runsWithDefaults() {
		SettlementAdminBatchRunRequest request = new SettlementAdminBatchRunRequest("2025-04", null, null, 10L, 50);
		SettlementBatchResponse createResponse = batchResponseFixture(99L, "2025-04");
		when(settlementBatchService.createBatch(any(SettlementBatchCreateCommand.class)))
				.thenReturn(createResponse);
		when(settlementBatchService.getBatch(99L)).thenReturn(createResponse);

		ResponseEntity<ApiResponse<SettlementBatchResponse>> result =
				settlementAdminController.runBatch(adminMember(), request);

		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().id()).isEqualTo(99L);

		ArgumentCaptor<SettlementBatchCreateCommand> createCaptor =
				ArgumentCaptor.forClass(SettlementBatchCreateCommand.class);
		verify(settlementBatchService).createBatch(createCaptor.capture());
		assertThat(createCaptor.getValue().periodYm()).isEqualTo("2025-04");

		ArgumentCaptor<SettlementBatchRunCommand> commandCaptor =
				ArgumentCaptor.forClass(SettlementBatchRunCommand.class);
		verify(settlementBatchRunner).run(eq(99L), commandCaptor.capture());
		SettlementBatchRunCommand command = commandCaptor.getValue();
		assertThat(command.periodYm()).isEqualTo("2025-04");
		assertThat(command.startDate()).isEqualTo("2025-04-01");
		assertThat(command.endDate()).isEqualTo("2025-04-30");
		assertThat(command.sellerId()).isEqualTo(10L);
		assertThat(command.pageSize()).isEqualTo(50);
	}

	private CustomMember adminMember() {
		return new CustomMember(1L, "ADMIN");
	}

	private CustomMember sellerMember() {
		return new CustomMember(2L, "SELLER");
	}

	private SellerSettlementResponse responseFixture(Long settlementId, String periodYm) {
		SettlementBatch batch = SettlementPayoutFixture.newBatch(periodYm);
		SellerSettlement settlement = SettlementPayoutFixture.newSettlement(
				batch,
				10L,
				periodYm,
				new java.math.BigDecimal("10000"),
				new java.math.BigDecimal("1000"),
				SellerSettlementStatus.READY
		);
		ReflectionTestUtils.setField(settlement, "id", settlementId);
		return SellerSettlementResponse.from(settlement);
	}

	private SettlementBatchResponse batchResponseFixture(Long batchId, String periodYm) {
		return new SettlementBatchResponse(
				batchId,
				periodYm,
				SettlementBatchStatus.READY,
				null,
				null,
				null,
				null
		);
	}
}
