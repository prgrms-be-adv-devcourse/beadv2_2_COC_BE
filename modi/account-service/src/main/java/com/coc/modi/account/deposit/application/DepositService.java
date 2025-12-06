package com.coc.modi.account.deposit.application;

import com.coc.modi.account.deposit.application.dto.DepositCommand;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final PgDepositRepository pgDepositRepository;
    private static final String PG_PROVIDER = "TOSS_PAYMENTS";

    // 예치금 충전 요청
    public DepositResponse requestDeposit(DepositCommand command) {

        String orderId = generateOrderId();

        PgDeposit pgDeposit = PgDeposit.createRequest(
                command.memberId(),
                command.amount(),
                PG_PROVIDER,
                orderId
        );

        PgDeposit save = pgDepositRepository.save(pgDeposit);

        return DepositResponse.from(save);
    }

    // 주문번호 생성
    public String generateOrderId() {
        return "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
