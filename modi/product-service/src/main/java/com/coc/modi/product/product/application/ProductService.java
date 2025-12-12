package com.coc.modi.product.product.application;

import com.coc.modi.product.product.application.dto.ProductBulkResponse;
import com.coc.modi.product.product.application.dto.ProductCommand;
import com.coc.modi.product.product.application.dto.ProductResponse;
import com.coc.modi.product.product.application.dto.ProductScrollResponse;
import com.coc.modi.product.product.application.dto.ProductSearchCondition;
import com.coc.modi.product.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageSpec;
import com.coc.modi.product.product.domain.ProductRepository;
import com.coc.modi.product.product.domain.ProductStatus;
import com.coc.modi.product.product.exception.ProductAccessDeniedException;
import com.coc.modi.product.product.exception.ProductNotFoundException;
import com.coc.modi.product.product.infrastructure.client.SellerFeignClient;
import com.coc.modi.product.search.application.ProductIndexService;
import com.coc.modi.product.search.application.ProductSearchPort;
import com.coc.modi.product.search.domain.ProductSortType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;
	private final ProductSearchPort searchPort;
	private final SellerFeignClient sellerFeignClient;
	private final ProductIndexService indexService;
	
	// 3-1. 상품 목록 조회 검색 기능
    @Transactional(readOnly = true)
    public ProductScrollResponse searchProducts(ProductSearchCondition condition, String cursor, int size, ProductSortType sortType) {
		
		return searchPort.searchProducts(condition, cursor, size, sortType);
    }

    // 3-2. 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductResponse getProductDetail(Long productId) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

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
                .orElseThrow(() -> new ProductNotFoundException(productId));
		
		if (!sellerId.equals(product.getSellerId())) {
			throw new ProductAccessDeniedException("수정");
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
                .orElseThrow(() -> new ProductNotFoundException(productId));
		
		Long sellerId = sellerFeignClient.findSellerById(memberId).sellerId();
		
		if (!sellerId.equals(product.getSellerId())) {
			throw new ProductAccessDeniedException("상태 변경");
		}
		
        product.updateStatus(status);

        if (status == ProductStatus.DELETE) {
            // 완전 삭제 → ES에서도 제거
			searchPort.deleteById(productId);
        } else {
            // ACTIVE/INACTIVE 등의 상태 변경 → ES 문서 갱신
			indexService.index(product);
        }
    }
}
