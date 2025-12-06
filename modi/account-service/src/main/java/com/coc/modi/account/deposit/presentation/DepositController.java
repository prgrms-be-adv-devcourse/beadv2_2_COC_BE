package com.coc.modi.account.deposit.presentation;

import com.coc.modi.account.deposit.application.DepositService;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.presentation.dto.DepositRequest;
import com.coc.modi.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits/pg")
public class DepositController {

    private final DepositService depositService;
    
    // 예치금 충전 요청
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<DepositResponse>> requestDeposit(@RequestBody DepositRequest request,
                                                                       Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        DepositResponse response = depositService.requestDeposit(request.toCommand(memberId));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
