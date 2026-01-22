package com.coc.modi.account.withdrawal.presentation;

import com.coc.modi.account.withdrawal.application.WithdrawalService;
import com.coc.modi.account.withdrawal.application.dto.WithdrawalResponse;
import com.coc.modi.account.withdrawal.presentation.dto.WithdrawalCreateRequest;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    public ResponseEntity<ApiResponse<WithdrawalResponse>> requestWithdrawal(
            @AuthenticationPrincipal CustomMember member,
            @Valid @RequestBody WithdrawalCreateRequest request
    ) {

        WithdrawalResponse response = withdrawalService.requestWithdrawal(member.memberId(), request.amount());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
