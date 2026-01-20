package com.coc.modi.seller.settlement.presentation.admin;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.exception.SettlementAccessDeniedException;
import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;
import com.coc.modi.seller.settlement.presentation.admin.dto.SettlementAdminBatchRunRequest;
import com.coc.modi.seller.settlement.presentation.admin.dto.SettlementBulkPayRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlements")
public class SettlementAdminController {

	private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final DateTimeFormatter PAID_AT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	private final SellerSettlementService sellerSettlementService;
	private final SettlementBatchService settlementBatchService;
	private final SettlementBatchRunner settlementBatchRunner;

	@GetMapping("/seller-settlements")
	public ResponseEntity<ApiResponse<Page<SellerSettlementResponse>>> getSettlements(
			@AuthenticationPrincipal CustomMember member,
			@RequestParam(value = "periodYm", required = false) String periodYm,
			@RequestParam(value = "sellerId", required = false) Long sellerId,
			@RequestParam(value = "status", required = false) SellerSettlementStatus status,
			Pageable pageable
	) {

		requireAdmin(member);
		Page<SellerSettlementResponse> response = sellerSettlementService
				.getSettlementsForAdmin(sellerId, periodYm, status, pageable);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PostMapping("/seller-settlements/{sellerSettlementId}/pay")
	public ResponseEntity<ApiResponse<SellerSettlementResponse>> paySettlement(
			@AuthenticationPrincipal CustomMember member,
			@PathVariable Long sellerSettlementId,
			@RequestParam(value = "paidAt", required = false) String paidAt
	) {

		requireAdmin(member);
		LocalDateTime paidAtValue = paidAt != null ? parsePaidAt(paidAt) : LocalDateTime.now();
		SellerSettlementResponse response = sellerSettlementService.requestPayoutByAdmin(sellerSettlementId, paidAtValue);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PostMapping("/seller-settlements/pay-bulk")
	public ResponseEntity<ApiResponse<SettlementBulkPayResponse>> payBulk(
			@AuthenticationPrincipal CustomMember member,
			@RequestBody SettlementBulkPayRequest request
	) {

		requireAdmin(member);
		if (request == null) {
			throw new SettlementInputInvalidException("요청 본문이 비어 있습니다.");
		}

		SellerSettlementStatus status = request.status() != null
				? request.status()
				: SellerSettlementStatus.FAILED;
		LocalDateTime paidAtValue = request.paidAt() != null ? parsePaidAt(request.paidAt()) : LocalDateTime.now();

		SettlementBulkPayResponse response = sellerSettlementService.requestPayoutsByAdmin(
				request.sellerId(),
				request.periodYm(),
				status,
				paidAtValue
		);
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	@PostMapping("/batches/run")
	public ResponseEntity<ApiResponse<SettlementBatchResponse>> runBatch(
			@AuthenticationPrincipal CustomMember member,
			@RequestBody SettlementAdminBatchRunRequest request
	) {

		requireAdmin(member);
		if (request == null || !StringUtils.hasText(request.periodYm())) {
			throw new SettlementInputInvalidException("periodYm은 필수입니다.");
		}

		YearMonth period = parsePeriod(request.periodYm());
		String startDate = resolveDate(request.startDate(), period.atDay(1).toString());
		String endDate = resolveDate(request.endDate(), period.atEndOfMonth().toString());

		SettlementBatchResponse batch = settlementBatchService.createBatch(
				new SettlementBatchCreateCommand(request.periodYm())
		);

		settlementBatchRunner.run(batch.id(), new SettlementBatchRunCommand(
				request.periodYm(),
				startDate,
				endDate,
				request.sellerId(),
				request.pageSize()
		));

		return ResponseEntity.ok(ApiResponse.ok(settlementBatchService.getBatch(batch.id())));
	}

	private void requireAdmin(CustomMember member) {

		if (member == null || !"ADMIN".equals(member.role())) {
			throw new SettlementAccessDeniedException("관리자 권한이 필요합니다.");
		}
	}

	private YearMonth parsePeriod(String periodYm) {

		try {
			return YearMonth.parse(periodYm, PERIOD_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new SettlementInputInvalidException("periodYm은 yyyy-MM 형식이어야 합니다.", e);
		}
	}

	private String resolveDate(String requested, String defaultValue) {

		return StringUtils.hasText(requested) ? requested : defaultValue;
	}

	private LocalDateTime parsePaidAt(String paidAt) {

		try {
			return LocalDateTime.parse(paidAt, PAID_AT_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new SettlementInputInvalidException(
					"paidAt must be ISO-8601 format, e.g. 2024-12-31T23:59:59",
					e
			);
		}
	}
}
