package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.rental.domain.*;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.infrastructure.client.AccountFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.WalletInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalPaymentService {

    private final RentalRepository rentalRepository;
    private final AccountFeignClient accountFeignClient;
    private final RentalEventLogService rentalEventLogService;

    @Transactional
    public PayRentalResponse completePayment(Long rentalId, Long memberId) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 정보를 찾을 수 없습니다. rentalId: " + rentalId));

        if (!rental.getMemberId().equals(memberId)) {

            throw new IllegalArgumentException("대여 요청자와 요청 멤버 정보가 일치하지 않습니다. rentalId: " + rentalId);
        }

        if (rental.getItems() == null || rental.getItems().isEmpty()) {

            throw new IllegalStateException("결제할 대여 상품이 없습니다. rentalId: " + rentalId);
        }

        rental.updateStatusFromItems();
        RentalStatus rentalStatus = rental.getStatus();

        if (rentalStatus == RentalStatus.CANCELED || rentalStatus == RentalStatus.COMPLETED) {

            throw new IllegalStateException("취소되었거나 완료된 대여는 결제할 수 없습니다. rentalId: " + rentalId + ", rentalStatus: " + rentalStatus);
        }

        if (rentalStatus != RentalStatus.ACCEPTED) {

            throw new IllegalStateException("모든 대여 상품이 승인 상태일 때만 결제할 수 있습니다. rentalId: " + rentalId + ", rentalStatus: " + rentalStatus);
        }

        BigDecimal totalAmount = rental.getTotalAmount();
        WalletInfoResponse walletInfoResponse = accountFeignClient.charge(new ChargeWalletCommand(
                memberId,
                rental.getId(),
                totalAmount
        ));

        LocalDateTime paidAt = LocalDateTime.now();
        rental.markPaid(paidAt);

        rentalEventLogService.logEvent(rental, RentalEventType.PAID, Map.of(
                "rentalId", rental.getId(),
                "paidAt", paidAt,
                "amount", totalAmount,
                "walletBalance", walletInfoResponse.balance(),
                "rentalStatus", rental.getStatus().name()
        ));

        return PayRentalResponse.create(rental, totalAmount, walletInfoResponse.balance(), paidAt);
    }
}
