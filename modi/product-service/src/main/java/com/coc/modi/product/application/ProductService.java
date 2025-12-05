package com.coc.modi.product.application;

import com.coc.modi.product.application.dto.*;
import com.coc.modi.product.domain.*;
import com.coc.modi.product.infrastructure.ProductImageJpaRepository;
import com.coc.modi.product.search.ProductDocument;
import com.coc.modi.product.search.ProductSearchQueryRepository;
import com.coc.modi.product.search.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    // TODO: 사용자 검증 로직 추가

    private final ProductRepository repository;
    private final ProductImageJpaRepository imageJpaRepository;
    private final ProductSearchRepository searchRepository;
    private final ProductSearchQueryRepository searchQueryRepository;

    // 3-1. 상품 목록 조회 기본 조회만
    @Transactional(readOnly = true)
    public List<ProductListResponse> getProducts(Pageable pageable) {

        Page<ProductDocument> docs = searchRepository.findByStatus(ProductStatus.ACTIVE.name(), pageable);

        return docs.map(ProductListResponse::from).getContent();
    }

    // 3-1. 상품 목록 조회 검색 기능
    @Transactional(readOnly = true)
    public List<ProductListResponse> searchProducts(ProductSearchCondition condition, Pageable pageable) {

        if (condition == null || condition.isEmpty()) {
            return getProducts(pageable);
        }

        Page<ProductDocument> docs = searchQueryRepository.search(condition, pageable);

        return docs.map(ProductListResponse::from).getContent();
    }

    // 3-2. 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductResponse getProductDetail(Long productId) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));

        return ProductResponse.from(product);
    }

    // 3-3. 상품 이미지 등록
    @Transactional
    public String uploadImage(MultipartFile file) {

        // TODO: S3
        String url = "https://modi.com/image.jpg";

        return url;
    }

    // 3-4. 상품 등록
    @Transactional
    public ProductResponse createProduct(Long sellerId, ProductCommand command) {

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
        indexToSearch(saved);

        return ProductResponse.from(saved);
    }

    // 3-5. 상품 수정
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateCommand command) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));

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

        indexToSearch(product);

        return ProductResponse.from(product);
    }

    // 3-6. 상품 숨김
    @Transactional
    public void disableProduct(Long productId) {

        changeStatus(productId, ProductStatus.INACTIVE);
    }

    // 3-7. 상품 삭제
    @Transactional
    public void deleteProduct(Long productId) {

        changeStatus(productId, ProductStatus.DELETE);
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

    // ES 인덱싱 공통 로직
    private void indexToSearch(Product product) {

        String thumbnailUrl = resolveThumbnailUrl(product);
        ProductDocument doc = ProductDocument.from(product, thumbnailUrl);

        searchRepository.save(doc);
    }

    // 썸네일 URL 구하기 (단건)
    private String resolveThumbnailUrl(Product product) {

        Long thumbnailId = product.getThumbnailImageId();

        if (thumbnailId == null) {
            return null;
        }

        return imageJpaRepository.findById(thumbnailId)
                .map(ProductImage::getUrl)
                .orElse(null);
    }

    // 상품 상태 변경
    private void changeStatus(Long productId, ProductStatus status) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));
        product.updateStatus(status);

        if (status == ProductStatus.DELETE) {
            // 완전 삭제 → ES에서도 제거
            searchRepository.deleteById(productId);
        } else {
            // ACTIVE/INACTIVE 등의 상태 변경 → ES 문서 갱신
            indexToSearch(product);
        }
    }
}
