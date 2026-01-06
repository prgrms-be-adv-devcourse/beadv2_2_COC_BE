package com.coc.modi.account.wallet.presentation;

import com.coc.modi.account.wallet.application.MemberWalletService;
import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.application.dto.MemberWalletResponse;
import com.coc.modi.account.wallet.application.dto.RentalPaymentCommand;
import com.coc.modi.account.wallet.application.dto.RentalPaymentResponse;
import com.coc.modi.account.wallet.application.dto.RentalRefundCommand;
import com.coc.modi.account.wallet.presentation.dto.RentalPaymentRequest;
import com.coc.modi.account.wallet.presentation.dto.RentalRefundRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/wallets")
public class WalletInternalController {

    private final WalletCommandService walletCommandService;
	private final MemberWalletService memberWalletService;

	// member-service 에서 회원탈퇴 시 호출 -> 지갑 잔액 확인
	@GetMapping("/{memberId}/balance")
	public MemberWalletResponse getMemberWalletBalance(@PathVariable Long memberId) {
		
		return memberWalletService.getMemberWalletBalance(memberId);
	}


    // rental-service 에서 호출 -> 지갑 결제 처리
    @PostMapping("/rental-payment")
    public RentalPaymentResponse payForRental(@Valid @RequestBody RentalPaymentRequest request) {

        RentalPaymentCommand command = RentalPaymentCommand.from(request);

        return walletCommandService.payForRental(command);
    }

    // rental-service 에서 호출 -> 지갑 환불 처리
    @PostMapping("/rental-refund")
    public RentalPaymentResponse refundForRental(@Valid @RequestBody RentalRefundRequest request) {

        RentalRefundCommand command = RentalRefundCommand.from(request);

        return walletCommandService.refundForRental(command);
    }
}
