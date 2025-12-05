package com.coc.modi.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.application.dto.RentalReturnCommand;
import com.coc.modi.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.domain.Rental;
import com.coc.modi.rental.domain.RentalEventType;
import com.coc.modi.rental.domain.RentalItem;
import com.coc.modi.rental.domain.RentalItemStatus;
import com.coc.modi.rental.domain.RentalStatus;
import com.coc.modi.rental.infrastructure.RentalRepository;
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
    public ResponseEntity<ApiResponse<RentalReturnResponse>> completeReturn(RentalReturnCommand command) {

        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 정보를 찾을 수 없습니다. rentalId: " + command.rentalId()));

        if (!rental.getMemberId().equals(command.memberId())) {

            throw new IllegalArgumentException("대여 요청자와 요청 멤버 정보가 일치하지 않습니다. rentalId: " + command.rentalId());
        }

        if (rental.getStatus() != RentalStatus.PAID && rental.getStatus() != RentalStatus.IN_PROGRESS) {

            throw new IllegalStateException("현재 상태에서 반납 처리가 불가능합니다. rentalStatus: " + rental.getStatus());
        }

        List<RentalItem> items = rental.getItems();

        if (items != null) {

            items.forEach(item -> {

                if (item.getStatus() == RentalItemStatus.CANCELED) {

                    return;
                }

                item.markReturned();
            });
        }

        rental.markReturned();

        BigDecimal extraFee = command.damageFee() == null ? BigDecimal.ZERO : command.damageFee();
        extraFee = extraFee.add(command.lateFee() == null ? BigDecimal.ZERO : command.lateFee());

        rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_RETURNED, Map.of(
                "rentalId", rental.getId(),
                "status", rental.getStatus().name(),
                "damageFee", command.damageFee(),
                "lateFee", command.lateFee(),
                "extraFeeTotal", extraFee,
                "damageReason", command.damageReason(),
                "lateReason", command.lateReason(),
                "memo", command.memo()
        ));

        return ResponseEntity.ok(ApiResponse.ok(new RentalReturnResponse(
                rental.getId(),
                rental.getStatus().name(),
                extraFee.toPlainString()
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

        // TODO: 결제 환불 연동이 준비되면 Account 서비스 호출 추가
        rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_REFUNDED, Map.of(
                "rentalId", rental.getId(),
                "status", rental.getStatus().name()
        ));

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
