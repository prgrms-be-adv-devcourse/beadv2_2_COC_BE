package com.coc.modi.seller.application.port;

import com.coc.modi.seller.infrastructure.client.rental.dto.RentalListResponse;

public interface RentalPort {

    RentalListResponse getRentals(Long sellerId,
                                  String status,
                                  String periodYm,
                                  String startDate,
                                  String endDate,
                                  Integer page,
                                  Integer size);
}
