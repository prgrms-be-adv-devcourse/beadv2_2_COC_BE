package com.coc.modi.seller.settlement.presentation;

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
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;
import com.coc.modi.seller.settlement.presentation.dto.SettlementBatchCreateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/settlements/batches")
public class SettlementBatchInternalController {
	
	private final SettlementBatchService settlementBatchService;
	
	@PostMapping
	public ApiResponse<SettlementBatchResponse> createBatch(@Valid @RequestBody SettlementBatchCreateRequest request) {
		
		return ApiResponse.ok(settlementBatchService.createBatch(request.toCommand()));
	}
	
	@PostMapping("/{batchId}/start")
	public ApiResponse<SettlementBatchResponse> startBatch(@PathVariable Long batchId) {
		
		return ApiResponse.ok(settlementBatchService.startBatch(batchId));
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
}
