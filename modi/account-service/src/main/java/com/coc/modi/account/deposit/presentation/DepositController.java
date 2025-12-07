package com.coc.modi.account.deposit.presentation;

import com.coc.modi.account.deposit.application.DepositService;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.infrastructure.config.TossPaymentsConfig;
import com.coc.modi.account.deposit.presentation.dto.DepositApprovalRequest;
import com.coc.modi.account.deposit.presentation.dto.DepositRequest;
import com.coc.modi.account.deposit.presentation.dto.TossConfigResponse;
import com.coc.modi.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits/pg")
public class DepositController {

    private final DepositService depositService;
    private final TossPaymentsConfig tossPaymentsConfig;
    
    // 예치금 충전 요청
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<DepositResponse>> requestDeposit(@RequestBody DepositRequest request,
                                                                       Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        DepositResponse response = depositService.requestDeposit(request.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 예치금 충전 승인
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<DepositResponse>> approveDeposit(@RequestBody DepositApprovalRequest request){

        DepositResponse response = depositService.approveDeposit(request.toCommand());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<TossConfigResponse>> tossConfig() {
        TossConfigResponse response = new TossConfigResponse(
                tossPaymentsConfig.getClientKey(),
                tossPaymentsConfig.getSuccessUrl(),
                tossPaymentsConfig.getFailUrl()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}
