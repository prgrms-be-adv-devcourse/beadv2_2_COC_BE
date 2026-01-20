package com.coc.modi.ai.embedding.infrastructure.client;

import com.coc.modi.ai.embedding.domain.ProductEmbeddingTarget;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "product-service",
        url = "${product-service.url}",
        path = "/internal/products"
)
public interface ProductEmbeddingClient {

    @GetMapping("/{productId}/embedding")
    ProductEmbeddingTarget getEmbeddingTarget(@PathVariable("productId") Long productId);

    @GetMapping("/embedding-ids")
    List<Long> getEmbeddingTargetIds();

    @GetMapping("/recent-viewed")
    List<Long> getRecentViewedProductIds(@RequestParam("memberId") Long memberId,
                                         @RequestParam(value = "limit", defaultValue = "10") int limit);
}
