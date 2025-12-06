package com.coc.modi.seller.infrastructure.client.rental;

import com.coc.modi.seller.infrastructure.client.rental.dto.RentalListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rentalClient", url = "${rental-service.base-url}")
public interface RentalClient {

    @GetMapping("/internal/rentals")
    RentalListResponse getRentals(@RequestParam("sellerId") Long sellerId,
                                  @RequestParam(value = "status", required = false) String status,
                                  @RequestParam(value = "periodYm", required = false) String periodYm,
                                  @RequestParam(value = "startDate", required = false) String startDate,
                                  @RequestParam(value = "endDate", required = false) String endDate,
                                  @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                  @RequestParam(value = "size", required = false, defaultValue = "20") Integer size);
}
