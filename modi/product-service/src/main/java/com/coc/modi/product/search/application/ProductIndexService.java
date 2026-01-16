package com.coc.modi.product.search.application;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.search.domain.ProductDocument;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductIndexService {
	
	private final ProductSearchPort productSearchPort;
	private final ProductDocumentMapper productDocumentMapper;
	
	public void index(Product product) {
		ProductDocument doc = productDocumentMapper.toDocument(product);
		if (doc == null) {
			return;
		}
		productSearchPort.index(doc);
	}

	public void deleteById(Long productId) {
		if (productId == null) {
			return;
		}
		productSearchPort.deleteById(productId);
	}
}
