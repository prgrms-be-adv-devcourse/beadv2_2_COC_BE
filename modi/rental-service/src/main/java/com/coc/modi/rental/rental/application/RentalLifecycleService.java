package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.dto.RentalReturnCommand;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.application.dto.WalletRefundResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.infrastructure.RentalItemRepository;
import com.coc.modi.rental.rental.infrastructure.RentalRepository;
import com.coc.modi.rental.rental.infrastructure.client.AccountFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.SellerFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.SellerInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalLifecycleService {

    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;
    private final SellerFeignClient sellerFeignClient;
    private final AccountFeignClient accountFeignClient;
    private final RentalEventLogService rentalEventLogService;

    @Transactional
    public ResponseEntity<ApiResponse<Void>> cancelRental(Long rentalId, Long memberId) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 정보를 찾을 수 없습니다. rentalId: " + rentalId));

        if (!rental.getMemberId().equals(memberId)) {

            throw new IllegalArgumentException("대여 요청자와 요청 멤버 정보가 일치하지 않습니다. rentalId: " + rentalId);
        }

        if (rental.getStatus() != RentalStatus.REQUESTED && rental.getStatus() != RentalStatus.PAID) {

            throw new IllegalStateException("현재 상태에서 대여 취소가 불가능합니다. rentalStatus: " + rental.getStatus());
        }

        List<RentalItem> items = rental.getItems();

        if (items != null) {

            items.forEach(RentalItem::markCanceled);
        }

        rental.markCanceled();

        rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_CANCELED, Map.of(
                "rentalId", rentalId,
                "status", rental.getStatus().name()
        ));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(Long rentalItemId, Long memberId, RentalReturnCommand command) {

        RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 물품 정보를 찾을 수 없습니다. rentalItemId: " + rentalItemId));

        SellerInfoResponse sellerInfoResponse = sellerFeignClient.getSellerInfo(rentalItem.getSellerId());

        if (!sellerInfoResponse.memberId().equals(memberId)) {

            throw new IllegalArgumentException("대여 물품 판매자와 현재 멤버 정보가 일치하지 않습니다. rentalItemId: " + rentalItemId);
        }

        Rental rental = rentalItem.getRental();

        if (rental == null) {

            throw new IllegalStateException("대여 정보가 존재하지 않습니다. rentalItemId: " + rentalItemId);
        }

        if (rentalItem.getStatus() != RentalItemStatus.RENTING && rentalItem.getStatus() != RentalItemStatus.PAID) {

            throw new IllegalStateException("현재 상태에서 반납 처리가 불가능합니다. itemStatus: " + rentalItem.getStatus());
        }

        rentalItem.markReturned();

        boolean allFinished = rental.getItems().stream()
                .allMatch(item -> item.getStatus() == RentalItemStatus.RETURNED
                        || item.getStatus() == RentalItemStatus.CANCELED
                        || item.getStatus() == RentalItemStatus.REJECTED);

        if (allFinished) {

            rental.markReturned();
        }

        BigDecimal damageFee = command.damageFee() == null ? BigDecimal.ZERO : command.damageFee();
        BigDecimal lateFee = command.lateFee() == null ? BigDecimal.ZERO : command.lateFee();

        rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_RETURNED, Map.of(
                "rentalId", rental.getId(),
                "rentalItemId", rentalItem.getId(),
                "rentalStatus", rental.getStatus().name(),
                "itemStatus", rentalItem.getStatus().name(),
                "damageFee", damageFee,
                "lateFee", lateFee,
                "extraFeeTotal", damageFee.add(lateFee),
                "damageReason", command.damageReason(),
                "lateReason", command.lateReason(),
                "memo", command.memo()
        ));

        return ResponseEntity.ok(ApiResponse.ok(new RentalReturnResponse(
                rental.getId(),
                rentalItem.getId(),
                rental.getStatus().name(),
                damageFee.add(lateFee).toPlainString()
        )));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> refundRental(Long rentalId, Long memberId) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 정보를 찾을 수 없습니다. rentalId: " + rentalId));

        if (!rental.getMemberId().equals(memberId)) {

            throw new IllegalArgumentException("대여 요청자와 요청 멤버 정보가 일치하지 않습니다. rentalId: " + rentalId);
        }

        if (rental.getStatus() != RentalStatus.CANCELED && rental.getStatus() != RentalStatus.COMPLETED) {

            throw new IllegalStateException("현재 상태에서 환불 처리가 불가능합니다. rentalStatus: " + rental.getStatus());
        }

        accountFeignClient.refund(new WalletRefundResponse());
        // TODO: 결제 환불 연동이 준비되면 Account 서비스 호출 추가
        rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_REFUNDED, Map.of(
                "rentalId", rental.getId(),
                "status", rental.getStatus().name()
        ));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
