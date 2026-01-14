package com.coc.modi.ai.embedding.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.ai.embedding.application.ProductEmbeddingReindexService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class ProductEmbeddingReindexController {

	private final ProductEmbeddingReindexService productEmbeddingReindexService;

	// 임베딩 미생성 건 재색인 (테스트/운영 보조)
	@PostMapping("/embeddings/reindex")
	public ResponseEntity<ApiResponse<Integer>> reindexEmbeddings() {

		int count = productEmbeddingReindexService.reindexMissing();
		return ResponseEntity.ok(ApiResponse.ok(count));
	}

	// 단건 임베딩 미생성 재색인
	@PostMapping("/{productId}/embedding")
	public ResponseEntity<ApiResponse<Boolean>> reindexEmbedding(@PathVariable("productId") Long productId) {

		return ResponseEntity.ok(ApiResponse.ok(productEmbeddingReindexService.reindexMissingOne(productId)));
	}
}
