package com.coc.modi.rental.infrastructure.client;

import com.coc.modi.rental.infrastructure.client.dto.ProductResponseDto;
import com.coc.modi.rental.infrastructure.client.dto.SellerInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "seller-service",
        url = "${seller-service.url}",
        path = "/internal/sellers"
)
public interface SellerFeignClient {

    @GetMapping
    SellerInfoResponse getSellerInfo(@RequestParam("sellerId") Long sellerId);
}
