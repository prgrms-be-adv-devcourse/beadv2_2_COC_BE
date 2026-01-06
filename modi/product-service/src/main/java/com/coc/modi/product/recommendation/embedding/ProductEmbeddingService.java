package com.coc.modi.product.recommendation.embedding;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;

import com.coc.modi.ai.embedding.EmbeddingClient;
import com.coc.modi.product.product.domain.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {
	
	private final EmbeddingClient embeddingClient;
	private final ProductEmbeddingRepository embeddingRepository;
	
	public void updateEmbedding(Product product) {
		
		if (product == null) {
			return;
		}
		
		String input = buildEmbeddingInput(product);
		try {
			List<Double> vector = embeddingClient.embed(input);
			embeddingRepository.updateEmbedding(product.getId(), vector);
		} catch (Exception ex) {
			log.warn("상품 임베딩 업데이트 실패. productId={}", product.getId(), ex);
		}
	}
	
	private String buildEmbeddingInput(Product product) {
		
		StringBuilder sb = new StringBuilder(256);
		sb.append(product.getName()).append('\n');
		sb.append(product.getDescription()).append('\n');
		sb.append("category: ").append(product.getCategory().name()).append('\n');
		
		Map<String, String> specs = product.getSpecs();
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
