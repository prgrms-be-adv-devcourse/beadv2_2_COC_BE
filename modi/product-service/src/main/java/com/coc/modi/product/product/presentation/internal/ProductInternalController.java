package com.coc.modi.product.product.presentation.internal;

import com.coc.modi.product.product.application.ProductService;
import com.coc.modi.product.product.application.dto.ProductBulkResponse;
import com.coc.modi.product.product.application.dto.ProductInternalSellerResponse;
import com.coc.modi.product.product.presentation.internal.dto.ProductEmbeddingResponse;
import com.coc.modi.product.viewlog.application.ProductViewService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/products")
public class ProductInternalController {
	
	private final ProductService productService;
	private final ProductViewService productViewService;
	
	@PostMapping("/bulk")
	public List<ProductBulkResponse> getProductsBulk(@RequestBody List<Long> productIds) {
		
		return productService.getProductsByIds(productIds);
	}
	
	@GetMapping("/{productId}")
	public ProductInternalSellerResponse getProductsById(@PathVariable("productId") Long productId) {
		
		return productService.getProductById(productId);
	}

	@GetMapping("/{productId}/embedding")
	public ProductEmbeddingResponse getEmbeddingTarget(@PathVariable("productId") Long productId) {
		
		return productService.getEmbeddingTarget(productId);
	}

	@GetMapping("/embedding-ids")
	public List<Long> getEmbeddingTargetIds() {
		
		return productService.getEmbeddingTargetIds();
	}

	@GetMapping("/recent-viewed")
	public List<Long> getRecentViewedProductIds(@RequestParam("memberId") Long memberId,
												@RequestParam(value = "limit", defaultValue = "10") int limit) {
		
		return productViewService.getRecentViewedProductIds(memberId, limit);
	}
}
