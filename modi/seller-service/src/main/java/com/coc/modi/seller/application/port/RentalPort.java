package com.coc.modi.seller.application.port;

import com.coc.modi.seller.infrastructure.client.rental.dto.RentalListResponse;

public interface RentalPort {

    RentalListResponse getRentals(Long sellerId,
                                  String status,
                                  String startDate,
                                  String endDate,
                                  Long productId,
                                  Integer page,
                                  Integer size);
}
