package com.coc.modi.product.product.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.product.product.application.support.SellerIdResolver;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.domain.ProductStatus;
import com.coc.modi.product.product.exception.ProductAccessDeniedException;
import com.coc.modi.product.product.exception.ProductNotFoundException;
import com.coc.modi.product.event.ProductIndexingEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductStatusService {
	
	private final ProductRepository productRepository;
	private final ProductIndexingEventPublisher productIndexingEventPublisher;
	private final SellerIdResolver sellerIdResolver;
	
	// 3-6. 상품 활성화
	@Transactional
	public void activeProduct(Long memberId, Long productId) {
		
		changeStatus(memberId, productId, ProductStatus.ACTIVE);
	}
	
	// 3-7. 상품 숨김
	@Transactional
	public void disableProduct(Long memberId, Long productId) {
		
		changeStatus(memberId, productId, ProductStatus.INACTIVE);
	}
	
	// 3-8. 상품 삭제
	@Transactional
	public void deleteProduct(Long memberId, Long productId) {
		
		changeStatus(memberId, productId, ProductStatus.DELETE);
	}
	
	// 상품 상태 변경
	private void changeStatus(Long memberId, Long productId, ProductStatus status) {
		
		Product product = productRepository.findByIdAndStatusNot(productId, ProductStatus.DELETE)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		
		Long sellerId = sellerIdResolver.getSellerId(memberId);
		
		if (!sellerId.equals(product.getSellerId())) {
			throw new ProductAccessDeniedException("상태 변경");
		}
		
		product.updateStatus(status);
		
		if (status == ProductStatus.DELETE) {
			// ES에서도 제거 이벤트 발행
			productIndexingEventPublisher.publishDelete(productId);
			productIndexingEventPublisher.publishEmbeddingUpdate(productId);
		} else {
			// ACTIVE/INACTIVE 등의 상태 변경 → ES 문서 갱신 이벤트 발행
			productIndexingEventPublisher.publishIndexAndEmbedding(product.getId());
		}
	}
}
