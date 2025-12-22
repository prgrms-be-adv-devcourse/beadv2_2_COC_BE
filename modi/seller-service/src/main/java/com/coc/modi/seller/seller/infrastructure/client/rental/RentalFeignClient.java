package com.coc.modi.seller.seller.infrastructure.client.rental;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.coc.modi.seller.seller.infrastructure.client.rental.dto.RentalListResponse;

@FeignClient(
        name = "rental-service",
        url = "${rental-service.url}",
        path = "/internal/rentals"
)
public interface RentalFeignClient {

    @GetMapping
    RentalListResponse getRentals(@RequestParam("sellerId") Long sellerId,
                                  @RequestParam(value = "productId", required = false) Long productId,
                                  @RequestParam("status") String status,
                                  @RequestParam("startDate") String startDate,
                                  @RequestParam("endDate") String endDate,
                                  @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                  @RequestParam(value = "size", required = false, defaultValue = "20") Integer size);
}
