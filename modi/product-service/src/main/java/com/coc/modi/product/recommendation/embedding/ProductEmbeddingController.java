package com.coc.modi.product.recommendation.embedding;

import com.coc.modi.common.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductEmbeddingController {

	private final ProductEmbeddingReindexService productEmbeddingReindexService;

	// 임베딩 재생성 (테스트/운영 보조)
	@PostMapping("/embeddings/reindex")
	public ResponseEntity<ApiResponse<Integer>> reindexEmbeddings() {

		int count = productEmbeddingReindexService.reindexAll();
		return ResponseEntity.ok(ApiResponse.ok(count));
	}

	// 단건 임베딩 재생성
	@PostMapping("/{productId}/embedding")
	public ResponseEntity<ApiResponse<Boolean>> reindexEmbedding(@PathVariable("productId") Long productId) {

		return ResponseEntity.ok(ApiResponse.ok(productEmbeddingReindexService.reindexOne(productId)));
	}
}
