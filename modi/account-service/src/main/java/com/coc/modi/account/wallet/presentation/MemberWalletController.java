package com.coc.modi.account.wallet.presentation;

import com.coc.modi.account.wallet.application.MemberWalletService;
import com.coc.modi.account.wallet.application.dto.MemberWalletResponse;
import com.coc.modi.account.wallet.application.dto.WalletTransactionResponse;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class MemberWalletController {

    private final MemberWalletService memberWalletService;

    // 예치금 조회
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<MemberWalletResponse>> getMemberWalletBalance(@AuthenticationPrincipal CustomMember member) {

        MemberWalletResponse response = memberWalletService.getMemberWalletBalance(member.getMemberId());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 예치금 거래 내역 조회
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> getWalletTransactions(@AuthenticationPrincipal CustomMember member) {

        List<WalletTransactionResponse> transactions = memberWalletService.getWalletTransactions(member.getMemberId());

        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }
}
