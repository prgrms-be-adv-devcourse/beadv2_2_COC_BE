package com.coc.modi.seller.settlement.presentation.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.settlement.application.SettlementBatchService;
import com.coc.modi.seller.settlement.application.SettlementBatchTriggerService;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;
import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;
import com.coc.modi.seller.settlement.presentation.dto.SettlementBatchCreateRequest;
import com.coc.modi.seller.settlement.presentation.dto.SettlementBatchMonthlyRunRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/settlements/batches")
public class SettlementBatchInternalController {
	
	private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	
	private final SettlementBatchService settlementBatchService;
	private final SettlementBatchTriggerService settlementBatchTriggerService;
	
	@PostMapping
	public ApiResponse<SettlementBatchResponse> createBatch(@Valid @RequestBody SettlementBatchCreateRequest request) {
		
		return ApiResponse.ok(settlementBatchService.createBatch(request.toCommand()));
	}
	
	@PostMapping("/{batchId}/start")
	public ApiResponse<SettlementBatchResponse> startBatch(@PathVariable Long batchId) {
		
		return ApiResponse.ok(settlementBatchService.startBatch(batchId));
	}
	
	@PostMapping("/monthly/run")
	public ApiResponse<SettlementBatchResponse> runMonthly(@RequestBody(required = false) SettlementBatchMonthlyRunRequest request) {
		
		YearMonth targetMonth = resolveTargetMonth(request == null ? null : request.periodYm());
		return ApiResponse.ok(settlementBatchTriggerService.runMonthly(targetMonth));
	}
	
	@PostMapping("/{batchId}/complete")
	public ApiResponse<SettlementBatchResponse> completeBatch(@PathVariable Long batchId) {
		
		return ApiResponse.ok(settlementBatchService.completeBatch(batchId));
	}
	
	@GetMapping
	public ApiResponse<Page<SettlementBatchResponse>> getBatches(@RequestParam(value = "periodYm", required = false) String periodYm,
																 Pageable pageable) {
		
		return ApiResponse.ok(settlementBatchService.getBatches(periodYm, pageable));
	}
	
	@GetMapping("/{batchId}")
	public ApiResponse<SettlementBatchResponse> getBatch(@PathVariable Long batchId) {
		
		return ApiResponse.ok(settlementBatchService.getBatch(batchId));
	}
	
	private YearMonth resolveTargetMonth(String periodYm) {
		
		if (periodYm == null || periodYm.isBlank()) {
			return YearMonth.now().minusMonths(1);
		}
		try {
			return YearMonth.parse(periodYm, PERIOD_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new SettlementInputInvalidException("periodYm은 yyyy-MM 형식이어야 합니다.", e);
		}
	}
}
