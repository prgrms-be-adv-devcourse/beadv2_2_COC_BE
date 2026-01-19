package com.coc.modi.product.product.application;

import com.coc.modi.product.product.application.dto.ProductBulkResponse;
import com.coc.modi.product.product.application.dto.ProductCreateCommand;
import com.coc.modi.product.product.application.dto.ProductDetailResponse;
import com.coc.modi.product.product.application.dto.ProductInternalSellerResponse;
import com.coc.modi.product.product.application.dto.ProductListResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.product.application.support.SellerIdResolver;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageRepository;
import com.coc.modi.product.product.domain.ProductImageSpec;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.domain.ProductModerationStatus;
import com.coc.modi.product.product.domain.ProductStatus;
import com.coc.modi.product.product.exception.ProductAccessDeniedException;
import com.coc.modi.product.product.exception.ProductInvalidInputException;
import com.coc.modi.product.product.exception.ProductNotFoundException;
import com.coc.modi.product.product.presentation.internal.dto.ProductEmbeddingResponse;
import com.coc.modi.product.event.KafkaProductEmbeddingEventPublisher;
import com.coc.modi.product.product.search.application.ProductSearchPort;
import com.coc.modi.product.product.search.domain.ProductSortType;
import com.coc.modi.product.searchlog.application.ProductSearchLogService;
import com.coc.modi.product.viewlog.application.ProductViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
	
	private final ProductRepository productRepository;
	private final ProductSearchPort productSearchPort;
	private final SellerIdResolver sellerIdResolver;
	private final KafkaProductEmbeddingEventPublisher productEmbeddingEventPublisher;
	private final ProductImageRepository productImageRepository;
	private final ProductSearchLogService productSearchLogService;
	private final ProductViewService productViewService;
	
	// 3-1. 상품 목록 조회 검색 기능
	@Transactional(readOnly = true)
	public ProductScrollResponse searchProducts(ProductSearchCondition condition,
												String cursor,
												int size,
												ProductSortType sortType,
												Long memberId) {
		
		ProductScrollResponse response = productSearchPort.searchProducts(condition, cursor, size, sortType);
		try {
			productSearchLogService.recordSearchLog(condition, sortType, cursor, size, memberId);
		} catch (Exception e) {
			log.warn("상품 검색 로그 저장 실패. keyword={}", condition.keyword(), e);
		}
		return response;
	}
	
	// 사용자의 판매 리스트 조회
	public Page<ProductListResponse> searchSellerProducts(Long memberId, Pageable pageable) {
		
		Long sellerId = sellerIdResolver.getSellerId(memberId);
		
		Page<Product> products = productRepository.findNonDeletedBySellerId(sellerId, pageable);
		
		List<Long> thumbnailIds = products.getContent().stream().map(Product::getThumbnailImageId).toList();
		
		Map<Long, String> thumbnailUrlMap = productImageRepository.findUrlMapByIds(thumbnailIds);
		
		return products.map(p -> ProductListResponse.fromProduct(p, thumbnailUrlMap.get(p.getThumbnailImageId())));
		
	}
	
	// 3-2. 상품 상세 조회
	@Transactional(readOnly = true)
	public ProductDetailResponse getProductDetail(Long memberId, Long productId) {
		
		Product product = productRepository.findNonDeletedById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		
		if (product.getModerationStatus() != ProductModerationStatus.CLEAR) {
			
			Long sellerId = sellerIdResolver.getSellerId(memberId);
			
			if (!Objects.equals(product.getSellerId(), sellerId)) {
				
				throw new ProductAccessDeniedException("접근");
			}
		}
		
		if (product.getStatus() == ProductStatus.INACTIVE) {
			
			Long sellerId = sellerIdResolver.getSellerId(memberId);
			
			if (!Objects.equals(product.getSellerId(), sellerId)) {
				
				throw new ProductAccessDeniedException("접근");
			}
		}
		
		try {
			productViewService.recordView(productId, memberId);
		} catch (Exception e) {
			log.warn("상품 조회 로그 저장 실패. productId={}", productId, e);
		}

		return ProductDetailResponse.from(product);
	}
	
	// 3-4. 상품 등록
	@Transactional
	public ProductDetailResponse createProduct(ProductCreateCommand command) {
		
		Long sellerId = sellerIdResolver.getSellerId(command.memberId());
		
		Product product = Product.create(
				sellerId,
				command.name(),
				command.description(),
				command.pricePerDay(),
				command.category(),
				command.specs(),
				command.imageUrls());
		
		Product saved = productRepository.saveAndFlush(product);
		saved.refreshThumbnailImage();
		
		// 모더레이션 통과 후에만 인덱싱/임베딩 이벤트 발행
		if (saved.getModerationStatus() == ProductModerationStatus.CLEAR) {
			productEmbeddingEventPublisher.publishUpdate(saved.getId());
		}
		
		return ProductDetailResponse.from(saved);
	}
	
	// 3-5. 상품 수정
	@Transactional
	public ProductDetailResponse updateProduct(ProductUpdateCommand command) {
		
		Long sellerId = sellerIdResolver.getSellerId(command.memberId());
		
		Product product = productRepository.findNonDeletedById(command.productId())
				.orElseThrow(() -> new ProductNotFoundException(command.productId()));

		boolean moderationChanged = shouldModerate(product, command);
		
		if (!sellerId.equals(product.getSellerId())) {
			throw new ProductAccessDeniedException("수정");
		}
		
		product.update(command.name(),
				command.description(),
				command.pricePerDay(),
				command.category(),
				command.specs());
		
		//이미지 변경 사항 반영 (null값인 경우 이미지 변동사항 없음)
		if (command.images() != null) {
			
			product.syncImages(command.images().stream()
					.map(ProductImageSpec::from)
					.toList());
		}
		
		productRepository.flush();

		if (moderationChanged) {
			product.updateModerationStatus(ProductModerationStatus.PENDING);
		}
		
		product.refreshThumbnailImage();
		
		if (product.getModerationStatus() == ProductModerationStatus.CLEAR) {
			productEmbeddingEventPublisher.publishUpdate(product.getId());
		}
	
		return ProductDetailResponse.from(product);
	}
	
	// 내부 api
	@Transactional(readOnly = true)
	public List<ProductBulkResponse> getProductsByIds(List<Long> productIds) {
		
		Map<Long, Product> productMap = getProductMapByIds(productIds);
		
		return productIds.stream()
				.map(productMap::get)
				.map(ProductBulkResponse::from)
				.toList();
	}
	
	// 내부 api
	@Transactional(readOnly = true)
	public ProductInternalSellerResponse getProductById(Long productId) {
		
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		
		String thumbnailImageUrl = productImageRepository.findById(product.getThumbnailImageId())
				.map(ProductImage::getUrl)
				.orElse(null);
		
		return ProductInternalSellerResponse.from(product, thumbnailImageUrl);
	}

	// 내부 api
	@Transactional(readOnly = true)
	public ProductEmbeddingResponse getEmbeddingTarget(Long productId) {
		
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		
		return ProductEmbeddingResponse.from(product);
	}

	// 내부 api
	@Transactional(readOnly = true)
	public List<Long> getEmbeddingTargetIds() {
		
		return productRepository.findNonDeletedByModerationStatus(ProductModerationStatus.CLEAR).stream()
				.map(Product::getId)
				.toList();
	}
	
	private Map<Long, Product> getProductMapByIds(List<Long> productIds) {
		
		List<Product> products = findProductsByIds(productIds);
		
		return products.stream()
				.collect(Collectors.toMap(Product::getId, Function.identity(), (existing, ignore) -> existing));
	}
	
	private List<Product> findProductsByIds(List<Long> productIds) {
		
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
		
		return products;
	}

	private boolean shouldModerate(Product product, ProductUpdateCommand command) {

		if (!Objects.equals(product.getName(), command.name())) {
			return true;
		}
		if (!Objects.equals(product.getDescription(), command.description())) {
			return true;
		}
		if (!Objects.equals(product.getSpecs(), command.specs())) {
			return true;
		}
		return command.images() != null;
	}
}
