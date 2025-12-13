package com.coc.modi.seller.infrastructure.client.rental;

import com.coc.modi.seller.application.port.RentalPort;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalClientAdapter implements RentalPort {

    private final RentalClient rentalClient;

    @Override
    public RentalListResponse getRentals(Long sellerId,
                                         String status,
                                         String startDate,
                                         String endDate,
                                         Long productId,
                                         Integer page,
                                         Integer size) {
        return rentalClient.getRentals(sellerId, productId, status, startDate, endDate, page, size);
    }
}
