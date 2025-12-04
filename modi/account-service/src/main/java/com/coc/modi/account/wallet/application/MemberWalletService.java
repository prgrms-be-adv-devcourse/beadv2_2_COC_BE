package com.coc.modi.account.wallet.application;

import com.coc.modi.account.wallet.application.dto.MemberWalletResponse;
import com.coc.modi.account.wallet.domain.MemberWallet;
import com.coc.modi.account.wallet.infrastructure.MemberWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWalletService {

    private final MemberWalletRepository memberWalletRepository;

    @Transactional(readOnly = true)
    public MemberWalletResponse getMemberWalletBalance(Authentication authentication){

        Long memberId = (Long) authentication.getPrincipal();

        MemberWallet wallet = memberWalletRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원의 예치금을 찾을 수 없습니다."));

        return new MemberWalletResponse(
                wallet.getBalance(),
                wallet.getCreatedAt()
        );
    }
}
