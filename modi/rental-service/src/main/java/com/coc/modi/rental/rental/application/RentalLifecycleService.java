package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.dto.RentalReturnCommand;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.application.dto.ExtendRentalCommand;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalItemRepository;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.infrastructure.client.AccountFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.SellerFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.RefundWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.SellerInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    public ResponseEntity<ApiResponse<Void>> cancelRentalItem(Long rentalItemId, Long memberId) {

        RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 상품 정보를 찾을 수 없습니다. rentalItemId: " + rentalItemId));

        if (!rentalItem.getRental().getMemberId().equals(memberId)) {

            throw new IllegalArgumentException("대여 요청자와 취소 요청 멤버 정보가 일치하지 않습니다. rentalItemId: " + rentalItem);
        }

        rentalItem.cancelByMemberRequest();

        Rental rental = rentalItem.getRental();
        rental.updateStatusFromItems();

        rentalEventLogService.logEvent(rentalItem.getRental(), RentalEventType.RENTAL_CANCELED, Map.of(
                "rentalItemId", rentalItemId,
                "status", rentalItem.getStatus().name()
        ));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(RentalReturnCommand command) {

        RentalItem rentalItem = rentalItemRepository.findById(command.rentalItemId())
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 물품 정보를 찾을 수 없습니다. rentalItemId: " + command.rentalItemId()));

        SellerInfoResponse sellerInfoResponse = sellerFeignClient.getSellerInfo(rentalItem.getSellerId());

        if (!sellerInfoResponse.memberId().equals(command.memberId())) {

            throw new IllegalArgumentException("대여 물품 판매자와 현재 멤버 정보가 일치하지 않습니다. rentalItemId: " + command.rentalItemId());
        }

        Rental rental = rentalItem.getRental();

        if (rental == null) {

            throw new IllegalStateException("대여 정보가 존재하지 않습니다. rentalItemId: " + command.rentalItemId());
        }

        rentalItem.processReturn();
        rental.updateStatusFromItems();

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
    public ResponseEntity<ApiResponse<Void>> extendRentalItem(ExtendRentalCommand command) {

        RentalItem rentalItem = rentalItemRepository.findById(command.rentalItemId())
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 상품 정보를 찾을 수 없습니다. rentalItemId: " + command.rentalItemId()));
        Rental rental = rentalItem.getRental();

        if (rental == null) {

            throw new IllegalStateException("대여 정보가 존재하지 않습니다. rentalItemId: " + command.rentalItemId());
        }

        if (!rental.getMemberId().equals(command.memberId())) {

            throw new IllegalArgumentException("대여 요청자와 요청 멤버 정보가 일치하지 않습니다. rentalItemId: " + command.rentalItemId());
        }

        if (rentalItem.getStatus() != RentalItemStatus.RENTING && rentalItem.getStatus() != RentalItemStatus.ACCEPTED) {

            throw new IllegalStateException("진행 중이거나 승인된 상품만 연장할 수 있습니다. rentalItemId: " + command.rentalItemId() + ", status: " + rentalItem.getStatus());
        }

        LocalDate oldEndDate = rentalItem.getEndDate();

        BigDecimal extraAmount = rentalItem.extendRental(command.newEndDate());

        accountFeignClient.charge(new ChargeWalletCommand(
                command.memberId(),
                rental.getId(),
                rentalItem.getId(),
                extraAmount
        ));

        rental.updateTotalAmount(rental.getTotalAmount().add(extraAmount));
        rental.updateStatusFromItems();

        long extraDays = ChronoUnit.DAYS.between(oldEndDate, command.newEndDate());

        rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_EXTENDED, Map.of(
                "rentalId", rental.getId(),
                "rentalItemId", rentalItem.getId(),
                "oldEndDate", oldEndDate,
                "newEndDate", command.newEndDate(),
                "extraDays", extraDays,
                "extraAmount", extraAmount,
                "totalAmount", rental.getTotalAmount()
        ));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> refundRentalItem(Long rentalItemId, Long memberId) {

        RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 상품 정보를 찾을 수 없습니다. rentalItemId: " + rentalItemId));
        Rental rental = rentalItem.getRental();

        if (rental == null) {

            throw new IllegalStateException("대여 정보가 존재하지 않습니다. rentalItemId: " + rentalItemId);
        }

        if (!rental.getMemberId().equals(memberId)) {

            throw new IllegalArgumentException("대여 요청자와 요청 멤버 정보가 일치하지 않습니다. rentalItemId: " + rentalItemId);
        }

        BigDecimal refundAmount = rentalItem.processRefund();
        accountFeignClient.refund(new RefundWalletCommand(
                memberId,
                rental.getId(),
                rentalItem.getId(),
                refundAmount
        ));

        rental.updateStatusFromItems();

        rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_REFUNDED, Map.of(
                "rentalId", rental.getId(),
                "rentalItemId", rentalItem.getId(),
                "status", rental.getStatus().name(),
                "itemStatus", rentalItem.getStatus().name(),
                "refundAmount", refundAmount
        ));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
