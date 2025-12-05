package com.coc.modi.account.wallet.presentation;

import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.application.dto.RentalPaymentCommand;
import com.coc.modi.account.wallet.application.dto.RentalPaymentResponse;
import com.coc.modi.account.wallet.presentation.dto.RentalPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/wallets")
public class WalletInternalController {

    private final WalletCommandService walletCommandService;

    // member-service 에서 회원가입 시 호출 -> 지갑 생성
    @PostMapping("/{memberId}")
    public void createWallet(@PathVariable Long memberId){

        walletCommandService.createWalletForMember(memberId);
    }


    // rental-service 에서 호출 -> 지갑 결제 처리
    @PostMapping("/rental-payment")
    public RentalPaymentResponse payForRental(@RequestBody RentalPaymentRequest request){

        RentalPaymentCommand command = RentalPaymentCommand.from(request);

        return walletCommandService.payForRental(command);
    }
}
