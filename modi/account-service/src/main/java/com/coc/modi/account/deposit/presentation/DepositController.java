package com.coc.modi.account.deposit.presentation;

import com.coc.modi.account.deposit.application.DepositService;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.infrastructure.config.TossPaymentsConfig;
import com.coc.modi.account.deposit.presentation.dto.DepositApprovalRequest;
import com.coc.modi.account.deposit.presentation.dto.DepositCancelRequest;
import com.coc.modi.account.deposit.presentation.dto.DepositFailRequest;
import com.coc.modi.account.deposit.presentation.dto.DepositRequest;
import com.coc.modi.account.deposit.application.dto.TossConfigResponse;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits/pg")
public class DepositController {

    private final DepositService depositService;
    private final TossPaymentsConfig tossPaymentsConfig;
    
    // 예치금 충전 요청
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<DepositResponse>> requestDeposit(@Valid @RequestBody DepositRequest request,
																	   @AuthenticationPrincipal CustomMember member) {

        DepositResponse response = depositService.requestDeposit(request.toCommand(member.memberId()));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 예치금 충전 승인
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<DepositResponse>> approveDeposit(@Valid @RequestBody DepositApprovalRequest request,
																	   @AuthenticationPrincipal CustomMember member) {

        DepositResponse response = depositService.approveDeposit(member.memberId(), request.toCommand());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 프론트용 결제 위젯 초기화
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<TossConfigResponse>> tossConfig() {

        TossConfigResponse response = new TossConfigResponse(
                tossPaymentsConfig.getClientKey(),
                tossPaymentsConfig.getSuccessUrl(),
                tossPaymentsConfig.getFailUrl()
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 예치금 충전 취소(환불)
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<DepositResponse>> cancelDeposit(@Valid @RequestBody DepositCancelRequest request,
																	  @AuthenticationPrincipal CustomMember member) {

        DepositResponse response = depositService.cancelDeposit(request.toCommand(member.memberId()));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
	
	// 예치금 충전 실패
	@PostMapping("/payments/fail")
	public ResponseEntity<ApiResponse<DepositResponse>> failDeposit(@Valid @RequestBody DepositFailRequest request,
																	@AuthenticationPrincipal CustomMember member) {
		
		DepositResponse response = depositService.failDeposit(member.memberId(), request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}

}
