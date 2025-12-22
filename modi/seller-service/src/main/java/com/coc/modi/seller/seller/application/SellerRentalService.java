package com.coc.modi.seller.seller.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coc.modi.seller.seller.infrastructure.client.rental.RentalClientAdapter;
import com.coc.modi.seller.seller.infrastructure.client.rental.dto.RentalItemInfo;
import com.coc.modi.seller.seller.infrastructure.client.rental.dto.RentalListResponse;
import com.coc.modi.seller.seller.application.dto.SellerRentalResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerRentalService {

    private final RentalClientAdapter rentalClientAdapter;

    public List<SellerRentalResponse> getSellerRentals(Long sellerId,
                                                       Long productId,
                                                       String status,
                                                       String startDate,
                                                       String endDate,
                                                       Integer page,
                                                       Integer size) {
        RentalListResponse response = rentalClientAdapter.getRentals(
                sellerId,
                productId,
                status,
                startDate,
                endDate,
                page != null ? page : 0,
                size != null ? size : 20
        );
        List<RentalItemInfo> rentals = response.content();
        if (rentals == null || rentals.isEmpty()) {
            return List.of();
        }
        return rentals.stream()
                .map(this::toResponse)
                .toList();
    }

    private SellerRentalResponse toResponse(RentalItemInfo rental) {
        return new SellerRentalResponse(
                rental.rentalItemId(),
                rental.productId(),
                rental.memberId(),
                rental.sellerId(),
                rental.status(),
                rental.totalAmount(),
                rental.startDate(),
                rental.endDate(),
                rental.paidAt()
        );
    }
}
