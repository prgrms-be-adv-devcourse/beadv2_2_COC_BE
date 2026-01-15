package com.coc.modi.seller.settlement.presentation.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.settlement.application.SellerSettlementService;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutFixture;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.exception.SettlementAccessDeniedException;
import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SellerSettlementAdminControllerTest {

	private static final DateTimeFormatter PAID_AT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	@Mock
	private SellerSettlementService sellerSettlementService;

	@InjectMocks
	private SellerSettlementAdminController sellerSettlementAdminController;

	@Test
	void getSellerSettlements_requiresAdmin() {
		Pageable pageable = PageRequest.of(0, 10);

		assertThatThrownBy(() -> sellerSettlementAdminController.getSellerSettlements("2025-01", pageable, userAuth()))
				.isInstanceOf(SettlementAccessDeniedException.class);
	}

	@Test
	void getSellerSettlements_returnsPageForAdmin() {
		Pageable pageable = PageRequest.of(0, 10);
		SellerSettlementResponse response = responseFixture(1L, "2025-01");
		Page<SellerSettlementResponse> page = new PageImpl<>(List.of(response), pageable, 1);
		when(sellerSettlementService.getAllSettlements("2025-01", pageable))
				.thenReturn(page);

		ApiResponse<Page<SellerSettlementResponse>> result =
				sellerSettlementAdminController.getSellerSettlements("2025-01", pageable, adminAuth());

		assertThat(result.success()).isTrue();
		assertThat(result.data().getTotalElements()).isEqualTo(1);
		verify(sellerSettlementService).getAllSettlements("2025-01", pageable);
	}

	@Test
	void paySellerSettlement_requiresAdmin() {
		assertThatThrownBy(() -> sellerSettlementAdminController.paySellerSettlement(1L, null, userAuth()))
				.isInstanceOf(SettlementAccessDeniedException.class);
	}

	@Test
	void paySellerSettlement_rejectsInvalidPaidAt() {
		assertThatThrownBy(() -> sellerSettlementAdminController.paySellerSettlement(1L, "invalid", adminAuth()))
				.isInstanceOf(SettlementInputInvalidException.class);
	}

	@Test
	void paySellerSettlement_callsServiceForAdmin() {
		LocalDateTime paidAt = LocalDateTime.of(2025, 2, 3, 4, 5, 6);
		SellerSettlementResponse response = responseFixture(2L, "2025-02");
		when(sellerSettlementService.requestPayoutByAdmin(2L, paidAt))
				.thenReturn(response);

		ApiResponse<SellerSettlementResponse> result = sellerSettlementAdminController.paySellerSettlement(
				2L,
				paidAt.format(PAID_AT_FORMATTER),
				adminAuth()
		);

		assertThat(result.success()).isTrue();
		assertThat(result.data().id()).isEqualTo(2L);
		verify(sellerSettlementService).requestPayoutByAdmin(2L, paidAt);
	}

	private Authentication adminAuth() {
		return new UsernamePasswordAuthenticationToken(
				"admin",
				null,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
		);
	}

	private Authentication userAuth() {
		return new UsernamePasswordAuthenticationToken(
				"user",
				null,
				List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
		);
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
}
