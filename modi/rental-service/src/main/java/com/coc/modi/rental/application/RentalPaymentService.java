package com.coc.modi.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.domain.Rental;
import com.coc.modi.rental.domain.RentalEventType;
import com.coc.modi.rental.domain.RentalItem;
import com.coc.modi.rental.domain.RentalItemStatus;
import com.coc.modi.rental.domain.RentalStatus;
import com.coc.modi.rental.infrastructure.RentalRepository;
import com.coc.modi.rental.infrastructure.client.AccountFeignClient;
import com.coc.modi.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.infrastructure.client.dto.WalletInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalPaymentService {

    private final RentalRepository rentalRepository;
    private final AccountFeignClient accountFeignClient;
    private final RentalEventLogService rentalEventLogService;

    @Transactional
    public ResponseEntity<ApiResponse<PayRentalResponse>> completePayment(Long rentalId, Long memberId) {

        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new IllegalArgumentException("해당 대여 정보를 찾을 수 없습니다. rentalId: " + rentalId));
        RentalStatus previousStatus = rental.getStatus();

        if (!rental.getMemberId().equals(memberId)) {

            throw new IllegalArgumentException("대여 요청자와 요청 멤버 정보가 일치하지 않습니다. rentalId: " + rentalId);
        }

        if (rental.getStatus() != RentalStatus.REQUESTED) {

            throw new IllegalStateException("결제 가능한 상태가 아닙니다. rentalId: " + rentalId);
        }

        List<RentalItem> items = rental.getItems();

        if (items == null || items.isEmpty()) {

            throw new IllegalStateException("결제할 대여 상품이 없습니다. rentalId: " + rentalId);
        }

        boolean hasNonAccepted = items.stream()
                .anyMatch(item -> item.getStatus() != RentalItemStatus.ACCEPTED);

        if (hasNonAccepted) {

            throw new IllegalStateException("모든 대여 상품이 승인 상태가 아닙니다. rentalId: " + rentalId);
        }

        WalletInfoResponse walletInfoResponse = accountFeignClient.charge(new ChargeWalletCommand(memberId, rentalId, rental.getTotalAmount()));

        items.forEach(RentalItem::markPaid);
        rental.markPaid(LocalDateTime.now());

        rentalEventLogService.logEvent(rental, RentalEventType.PAID, Map.of(
                "previousStatus", previousStatus.name(),
                "newStatus", rental.getStatus().name(),
                "paidAt", rental.getPaidAt(),
                "totalAmount", rental.getTotalAmount(),
                "walletBalance", walletInfoResponse.balance()
        ));

        return ResponseEntity.ok(ApiResponse.ok(PayRentalResponse.create(rental, walletInfoResponse.balance())));
    }
}
