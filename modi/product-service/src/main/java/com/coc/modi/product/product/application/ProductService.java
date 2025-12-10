package com.coc.modi.product.product.application;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.product.product.exception.ProductException;

import com.coc.modi.product.product.application.dto.*;
import com.coc.modi.product.product.application.dto.ProductBulkResponse;
import com.coc.modi.product.product.application.dto.ProductCommand;
import com.coc.modi.product.product.application.dto.ProductListResponse;
import com.coc.modi.product.product.application.dto.ProductResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.product.application.dto.RentalResponse;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageSpec;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.domain.ProductStatus;
import com.coc.modi.product.product.infrastructure.client.RentalFeignClient;
import com.coc.modi.product.product.infrastructure.client.SellerFeignClient;
import com.coc.modi.product.product.presentation.dto.RentalRequest;
import com.coc.modi.product.search.ProductDocument;
import com.coc.modi.product.search.ProductIndexService;
import com.coc.modi.product.search.ProductSearchQueryRepository;
import com.coc.modi.product.search.ProductSearchRepository;
import com.coc.modi.product.search.ProductSortType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {
    // TODO: 사용자 검증 로직 추가

    private final ProductRepository repository;
    private final ProductSearchRepository searchRepository;
    private final ProductSearchQueryRepository searchQueryRepository;
    private final ProductIndexService indexService;
	private final SellerFeignClient sellerFeignClient;
	private final RentalFeignClient rentalFeignClient;

    // 3-1. 상품 목록 조회 검색 기능
    @Transactional(readOnly = true)
    public ProductScrollResponse searchProducts(ProductSearchCondition condition, String cursor, int size, ProductSortType sortType) {
		
		ProductSortType effectiveSortType = sortType != null ? sortType : condition.effectiveSortType();
		
		List<ProductListResponse> items = new ArrayList<>();
		String currentCursor = cursor;
		boolean hasMoreFromEs = true;
		int safetyLimit = 5;
		
		List<ProductDocument> lastDocsBatch = List.of();
		
		while (items.size() < size && hasMoreFromEs && safetyLimit-- > 0) {
			
			// ES에서 1차 검색
			List<ProductDocument> docs = searchQueryRepository.search(condition, currentCursor, size, effectiveSortType);
			
			if (docs.isEmpty()) {
				hasMoreFromEs = false;
				break;
			}
			
			lastDocsBatch = docs;
			
			List<ProductDocument> availableDocs = docs;
			
			// 렌탈 기간 필터가 있는 경우(대여 불가 상품 제거)
			if (condition.hasRentalPeriod()) {
				List<Long> productIds = docs.stream()
						.map(ProductDocument::getId)
						.toList();
				
				if(!productIds.isEmpty()) {
					RentalRequest request = new RentalRequest(condition.startDate(), condition.endDate(), productIds);
					
					RentalResponse rentalResponse = rentalFeignClient.unavailableProducts(request);
					
					Set<Long> unavailableIds = new HashSet<>(
							Optional.ofNullable(rentalResponse)
									.map(RentalResponse::unavailableProductIds)
									.orElseGet(List::of)
					);
					
					availableDocs = docs.stream()
							.filter(doc -> !unavailableIds.contains(doc.getId())).toList();
				}
			}
			
			// 대여 가능한 리스트만 item에 추가
			items.addAll(availableDocs.stream()
					.map(ProductListResponse::from)
					.toList());
			
			// cursor 갱신
			currentCursor = buildNextCursor(docs, effectiveSortType);
			hasMoreFromEs = docs.size() == size && currentCursor != null;
		}
		
		// items가 빈 경우
		if (items.isEmpty()) {
			return new ProductScrollResponse(List.of(), null, false);
		}
		
		// size보다 많이 쌓인 경우 잘라주기
		if (items.size() > size) {
			items = items.subList(0, size);
		}
		
		// 결과 반환
		String nextCursor = (hasMoreFromEs && !lastDocsBatch.isEmpty())
				? currentCursor
				: null;
		
		boolean hasNext = hasMoreFromEs;
		
		return new ProductScrollResponse(items, nextCursor, hasNext);
    }

    // 3-2. 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductResponse getProductDetail(Long productId) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + productId));

        return ProductResponse.from(product);
    }

    // 3-4. 상품 등록
    @Transactional
    public ProductResponse createProduct(Long memberId, ProductCommand command) {
		
		Long sellerId = sellerFeignClient.findSellerById(memberId).sellerId();
		
        Product product = Product.create(
                sellerId,
                command.name(),
                command.description(),
                command.pricePerDay(),
                ProductCategory.from(command.category()));

        // 이미지 추가
        addImages(product, command.imageUrls());

        Product saved = repository.saveAndFlush(product);
        updateThumbnailFromFirstImage(saved);

        // ES 인덱싱
        indexService.index(saved);

        return ProductResponse.from(saved);
    }

    // 3-5. 상품 수정
    @Transactional
    public ProductResponse updateProduct(Long memberId, Long productId, ProductUpdateCommand command) {
		
		Long sellerId = sellerFeignClient.findSellerById(memberId).sellerId();

        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + productId));
		
		if (!sellerId.equals(product.getSellerId())) {
			throw new ProductException(ErrorCode.FORBIDDEN, "해당 상품의 수정 권한이 없습니다.");
		}

        product.update(command.name(),
                command.description(),
                command.pricePerDay(),
                ProductCategory.from(command.category()));

        //이미지 변경 사항 반영 (null값인 경우 이미지 변동사항 없음)
        if(command.images() != null) {
            product.syncImages(command.images().stream()
                    .map(ProductImageSpec::from)
                    .toList());
        }

        repository.saveAndFlush(product);

        indexService.index(product);

        return ProductResponse.from(product);
    }

    // 3-6. 상품 숨김
    @Transactional
    public void disableProduct(Long memberId, Long productId) {

        changeStatus(memberId, productId, ProductStatus.INACTIVE);
    }

    // 3-7. 상품 삭제
    @Transactional
    public void deleteProduct(Long memberId, Long productId) {

        changeStatus(memberId, productId, ProductStatus.DELETE);
    }

    // 내부 api
    @Transactional(readOnly = true)
    public List<ProductBulkResponse> getProductsByIds(List<Long> productIds) {

        return repository.findByIdIn(productIds)
                .stream()
                .map(ProductBulkResponse::from)
                .toList();
    }
	
	// 다음 커서
	private String buildNextCursor(List<ProductDocument> docs, ProductSortType sortType) {
		return switch (sortType) {
			case LATEST, OLDEST -> {
				// 뒤에서부터 createdAt 이 있는 문서를 찾음
				ProductDocument target = null;
				for (int i = docs.size() - 1; i >= 0; i--) {
					if (docs.get(i).getCreatedAt() != null) {
						target = docs.get(i);
						break;
					}
				}
				
				if (target == null) {
					// createdAt 이 하나도 없는 배치면 커서를 만들 수 없음
					yield null;
				}
				
				String rawCursor = target.getCreatedAt().toString();
				
				yield Base64.getUrlEncoder()
						.encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
			}
			case PRICE_HIGH, PRICE_LOW -> {
				ProductDocument target = null;
				for (int i = docs.size() - 1; i >= 0; i--) {
					if (docs.get(i).getPricePerDay() != null) {
						target = docs.get(i);
						break;
					}
				}
				
				if (target == null) {
					yield null;
				}
				
				BigDecimal price = target.getPricePerDay();
				Long id = target.getId();
				
				yield price.toPlainString() + ":" + id;
			}
		};
	}

    // 상품에 이미지 추가하기
    private void addImages(Product product, List<String> imageUrls) {

        product.updateThumbnailImageId(null);

        if(imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (int i=0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            int ordering = i + 1;

            ProductImage image = ProductImage.create(product, url, ordering);
            product.addImage(image);
        }
    }

    // 대표 이미지 설정 (첫 번째 이미지)
    private void updateThumbnailFromFirstImage(Product product) {

        if (product.getImages() == null || product.getImages().isEmpty()) {
            product.updateThumbnailImageId(null);

            return;
        }

        Long thumbnailId = product.getImages().get(0).getId();
        product.updateThumbnailImageId(thumbnailId);
    }

    // 상품 상태 변경
    private void changeStatus(Long memberId, Long productId, ProductStatus status) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + productId));
		
		Long sellerId = sellerFeignClient.findSellerById(memberId).sellerId();
		
		if (!sellerId.equals(product.getSellerId())) {
			throw new ProductException(ErrorCode.FORBIDDEN, "해당 상품의 상태 변경 권한이 없습니다.");
		}
		
        product.updateStatus(status);

        if (status == ProductStatus.DELETE) {
            // 완전 삭제 → ES에서도 제거
            searchRepository.deleteById(productId);
        } else {
            // ACTIVE/INACTIVE 등의 상태 변경 → ES 문서 갱신
            indexService.index(product);
        }
    }
}
