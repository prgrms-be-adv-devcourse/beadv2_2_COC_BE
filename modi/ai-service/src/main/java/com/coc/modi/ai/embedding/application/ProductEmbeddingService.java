package com.coc.modi.ai.embedding.application;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;

import com.coc.modi.ai.embedding.domain.EmbeddingClient;
import com.coc.modi.ai.embedding.domain.ProductEmbeddingTarget;
import com.coc.modi.ai.embedding.infrastructure.ProductEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {
	
	private final EmbeddingClient embeddingClient;
	private final ProductEmbeddingRepository embeddingRepository;
	
	public void updateEmbedding(ProductEmbeddingTarget target) {
		
		if (target == null) {
			return;
		}
		
		String input = buildEmbeddingInput(target);
		try {
			List<Double> vector = embeddingClient.embed(input);
			embeddingRepository.upsert(target, vector);
		} catch (Exception ex) {
			log.warn("상품 임베딩 업데이트 실패. productId={}", target.productId(), ex);
		}
	}
	
	private String buildEmbeddingInput(ProductEmbeddingTarget target) {
		
		StringBuilder sb = new StringBuilder(256);
		sb.append(target.name()).append('\n');
		sb.append(target.description()).append('\n');
		sb.append("category: ").append(target.category()).append('\n');
		
		Map<String, String> specs = target.specs();
		if (specs != null && !specs.isEmpty()) {
			StringJoiner joiner = new StringJoiner(", ");
			for (Map.Entry<String, String> entry : specs.entrySet()) {
				if (entry.getKey() == null || entry.getValue() == null) {
					continue;
				}
				joiner.add(entry.getKey() + "=" + entry.getValue());
			}
			if (joiner.length() > 0) {
				sb.append("specs: ").append(joiner).append('\n');
			}
		}
		
		return sb.toString();
	}
}
