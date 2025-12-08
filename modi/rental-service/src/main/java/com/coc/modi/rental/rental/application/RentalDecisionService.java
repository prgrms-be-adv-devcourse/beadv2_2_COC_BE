package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.domain.RentalItemRepository;
import com.coc.modi.rental.rental.infrastructure.client.SellerFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.SellerInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalDecisionService {

    private final RentalItemRepository rentalItemRepository;
    private final SellerFeignClient sellerFeignClient;

    @Transactional
    public void acceptRentalItem(Long rentalItemId, Long memberId) {

        decideRentalItem(rentalItemId, memberId, RentalItemStatus.ACCEPTED);
    }

    @Transactional
    public void rejectRentalItem(Long rentalItemId, Long memberId) {

        decideRentalItem(rentalItemId, memberId, RentalItemStatus.REJECTED);
    }

    private void decideRentalItem(Long rentalItemId, Long memberId, RentalItemStatus targetStatus) {

        RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대여 상품 정보를 찾을 수 없습니다. rentalItemId: " + rentalItemId));

        Rental rental = rentalItem.getRental();

        SellerInfoResponse sellerInfoResponse = sellerFeignClient.getSellerInfo(rentalItem.getSellerId());

        if (!sellerInfoResponse.memberId().equals(memberId)) {

            throw new IllegalArgumentException("현재 로그인한 판매자의 정보와 상품 판매자의 정보가 일치하지 않습니다.");
        }

        if (rental == null) {

            throw new IllegalStateException("대여 정보가 존재하지 않습니다. rentalItemId: " + rentalItemId);
        }

        RentalStatus rentalStatus = rental.getStatus();

        if (rentalStatus == RentalStatus.PAID
                || rentalStatus == RentalStatus.IN_PROGRESS
                || rentalStatus == RentalStatus.COMPLETED
                || rentalStatus == RentalStatus.CANCELED) {

            throw new IllegalArgumentException("현재 대여 정보의 상태에서 승인/거절이 불가능합니다. rentalStatus: " + rentalStatus);
        }

        rentalItem.decide(targetStatus);
        rental.updateStatusFromItems();
    }

}
