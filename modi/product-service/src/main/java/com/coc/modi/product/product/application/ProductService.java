package com.coc.modi.product.product.application;

import com.coc.modi.product.product.application.dto.ProductBulkResponse;
import com.coc.modi.product.product.application.dto.ProductCreateCommand;
import com.coc.modi.product.product.application.dto.ProductDetailResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductImageSpec;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.exception.ProductAccessDeniedException;
import com.coc.modi.product.product.exception.ProductInvalidInputException;
import com.coc.modi.product.product.exception.ProductNotFoundException;
import com.coc.modi.product.product.application.dto.SellerResponse;
import com.coc.modi.product.product.infrastructure.client.SellerFeignClient;
import com.coc.modi.product.search.application.ProductIndexService;
import com.coc.modi.product.search.application.ProductSearchPort;
import com.coc.modi.product.search.domain.ProductSortType;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
	
	private final ProductRepository productRepository;
	private final ProductSearchPort productSearchPort;
	private final SellerFeignClient sellerFeignClient;
	private final ProductIndexService productIndexService;
	
	// 3-1. 상품 목록 조회 검색 기능
	@Transactional(readOnly = true)
	public ProductScrollResponse searchProducts(ProductSearchCondition condition,
												String cursor,
												int size,
												ProductSortType sortType) {
		
		return productSearchPort.searchProducts(condition, cursor, size, sortType);
	}
	
	// 3-2. 상품 상세 조회
	@Transactional(readOnly = true)
	public ProductDetailResponse getProductDetail(Long productId) {
		
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		
		return ProductDetailResponse.from(product);
	}
	
	// 3-4. 상품 등록
	@Transactional
	public ProductDetailResponse createProduct(ProductCreateCommand command) {
		
		Long sellerId = getSellerId(command.memberId());
		
		Product product = Product.create(
				sellerId,
				command.name(),
				command.description(),
				command.pricePerDay(),
				command.category(),
				command.imageUrls());
		
		Product saved = productRepository.saveAndFlush(product);
		saved.refreshThumbnailImage();
		
		// ES 인덱싱
		productIndexService.index(saved);
		
		return ProductDetailResponse.from(saved);
	}
	
	// 3-5. 상품 수정
	@Transactional
	public ProductDetailResponse updateProduct(ProductUpdateCommand command) {
		
		Long sellerId = getSellerId(command.memberId());
		
		Product product = productRepository.findById(command.productId())
				.orElseThrow(() -> new ProductNotFoundException(command.productId()));
		
		if (!sellerId.equals(product.getSellerId())) {
			throw new ProductAccessDeniedException("수정");
		}
		
		product.update(command.name(),
				command.description(),
				command.pricePerDay(),
				command.category());
		
		boolean hasNewImage = false;
		
		//이미지 변경 사항 반영 (null값인 경우 이미지 변동사항 없음)
		if (command.images() != null) {
			
			hasNewImage = command.images().stream().anyMatch(spec -> spec.imageId() == null);
			
			product.syncImages(command.images().stream()
					.map(ProductImageSpec::from)
					.toList());
		}
		
		if (hasNewImage) {
			
			productRepository.flush();
		}
		
		product.refreshThumbnailImage();
		
		productIndexService.index(product);
		
		return ProductDetailResponse.from(product);
	}
	
	// sellerId 조회
	private Long getSellerId(Long memberId) {
		
		try {
			SellerResponse sellerResponse =
					sellerFeignClient.getSellerIdByMemberId(memberId);
			
			if (sellerResponse == null || sellerResponse.sellerId() == null) {
				throw new ProductInvalidInputException("판매자 정보를 찾을 수 없습니다. memberId: " + memberId);
			}
			
			return sellerResponse.sellerId();
			
		} catch (feign.FeignException.NotFound e) {
			
			throw new ProductInvalidInputException("판매자 정보가 등록되지 않았습니다. memberId: " + memberId);
			
		} catch (feign.FeignException e) {
			
			throw new ProductInvalidInputException("판매자 서비스 호출 중 오류가 발생했습니다.");
		}
	}
	
	// 내부 api
	@Transactional(readOnly = true)
	public List<ProductBulkResponse> getProductsByIds(List<Long> productIds) {
		
		if (productIds == null || productIds.isEmpty()) {
			throw new ProductInvalidInputException("조회할 상품 ID가 없습니다.");
		}
		
		List<Product> products = productRepository.findByIdIn(productIds);
		
		Set<Long> foundIds = products.stream()
				.map(Product::getId)
				.collect(Collectors.toSet());
		
		productIds.stream()
				.filter(id -> !foundIds.contains(id))
				.findFirst()
				.ifPresent(id -> {
					throw new ProductNotFoundException(id);
				});
		
		return products.stream()
				.map(ProductBulkResponse::from)
				.toList();
	}
}
