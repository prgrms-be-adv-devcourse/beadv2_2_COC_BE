package com.coc.modi.account.wallet.presentation;

import com.coc.modi.account.wallet.application.MemberWalletService;
import com.coc.modi.account.wallet.application.dto.MemberWalletResponse;
import com.coc.modi.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class MemberWalletController {

    private final MemberWalletService memberWalletService;

    @GetMapping("/balance")
    public ApiResponse<MemberWalletResponse> getMemberWalletBalance(Authentication authentication){

        MemberWalletResponse response = memberWalletService.getMemberWalletBalance(authentication);

        return ApiResponse.ok(response);
    }
}
