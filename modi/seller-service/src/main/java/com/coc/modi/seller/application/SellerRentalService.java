package com.coc.modi.seller.application;

import com.coc.modi.seller.application.dto.SellerRentalResponse;
import com.coc.modi.seller.infrastructure.client.rental.RentalFeignClient;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalItemInfo;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerRentalService {

	private final RentalFeignClient rentalFeignClient;

    public List<SellerRentalResponse> getSellerRentals(Long sellerId,
                                                       Long productId,
                                                       String status,
                                                       String startDate,
                                                       String endDate,
                                                       Integer page,
                                                       Integer size) {
		
        RentalListResponse response = rentalFeignClient.getRentals(
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
