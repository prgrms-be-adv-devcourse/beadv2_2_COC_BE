package com.coc.modi.seller.settlement.presentation.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.seller.settlement.application.SettlementBatchService;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/settlements/batches")
public class SettlementBatchInternalController {

	private final SettlementBatchService settlementBatchService;
	
	@GetMapping
	public ApiResponse<Page<SettlementBatchResponse>> getBatches(@RequestParam(value = "periodYm", required = false) String periodYm,
																 Pageable pageable) {
		
		return ApiResponse.ok(settlementBatchService.getBatches(periodYm, pageable));
	}
	
	@GetMapping("/{batchId}")
	public ApiResponse<SettlementBatchResponse> getBatch(@PathVariable Long batchId) {
		
		return ApiResponse.ok(settlementBatchService.getBatch(batchId));
	}
}
