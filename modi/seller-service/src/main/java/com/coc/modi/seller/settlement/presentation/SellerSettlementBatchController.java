package com.coc.modi.seller.settlement.presentation;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;
import com.coc.modi.seller.seller.application.SellerService;
import com.coc.modi.seller.seller.application.dto.SellerDetailResponse;
import com.coc.modi.seller.settlement.application.SettlementBatchRunner;
import com.coc.modi.seller.settlement.application.SettlementBatchService;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchCreateCommand;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;
import com.coc.modi.seller.settlement.presentation.dto.SellerSettlementBatchRunRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements/sellers/self/batches")
public class SellerSettlementBatchController {
	
	private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	
	private final SellerService sellerService;
	private final SettlementBatchService settlementBatchService;
	private final SettlementBatchRunner settlementBatchRunner;
	
	@PostMapping("/run")
	public ResponseEntity<ApiResponse<SettlementBatchResponse>> sellerSettlementRun(@AuthenticationPrincipal CustomMember member,
																					 @Valid @RequestBody SellerSettlementBatchRunRequest request) {
		
		SellerDetailResponse seller = sellerService.getSellerByMemberId(member.memberId());
		YearMonth period = parsePeriod(request.periodYm());
		String startDate = resolveDate(request.startDate(), period.atDay(1).toString());
		String endDate = resolveDate(request.endDate(), period.atEndOfMonth().toString());
		
		SettlementBatchResponse batch = settlementBatchService.createBatch(new SettlementBatchCreateCommand(request.periodYm()));
		
		settlementBatchRunner.run(batch.id(), request.toCommand(
				seller.sellerId(),
				startDate,
				endDate
		));
		
		return ResponseEntity.ok(ApiResponse.ok(settlementBatchService.getBatch(batch.id())));
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
}
