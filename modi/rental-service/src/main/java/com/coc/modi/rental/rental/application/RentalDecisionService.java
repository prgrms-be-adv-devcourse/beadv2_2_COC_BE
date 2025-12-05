package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.infrastructure.RentalItemRepository;
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
    public ResponseEntity<ApiResponse<Void>> approveRentalItem(Long rentalItemId, Long memberId) {

        return decideRentalItem(rentalItemId, memberId, RentalItemStatus.ACCEPTED);
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> declineRentalItem(Long rentalItemId, Long memberId) {

        return decideRentalItem(rentalItemId, memberId, RentalItemStatus.REJECTED);
    }

    private ResponseEntity<ApiResponse<Void>> decideRentalItem(Long rentalItemId, Long memberId, RentalItemStatus targetStatus) {

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

        if (rental.getStatus() != RentalStatus.REQUESTED) {

            throw new IllegalArgumentException("현재 대여 정보의 상태가 REQUESTED가 아닙니다. rentalStatus: " + rental.getStatus());
        }

        rentalItem.decide(targetStatus);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
