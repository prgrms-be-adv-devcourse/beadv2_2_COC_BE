package com.coc.modi.account.wallet.application;

import com.coc.modi.account.wallet.application.dto.MemberWalletResponse;
import com.coc.modi.account.wallet.domain.MemberWallet;
import com.coc.modi.account.wallet.domain.MemberWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWalletService {

    private final MemberWalletRepository memberWalletRepository;

    @Transactional(readOnly = true)
    public MemberWalletResponse getMemberWalletBalance(Long memberId){

        MemberWallet wallet = memberWalletRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원의 예치금을 찾을 수 없습니다."));

        return MemberWalletResponse.from(wallet);
    }
}
