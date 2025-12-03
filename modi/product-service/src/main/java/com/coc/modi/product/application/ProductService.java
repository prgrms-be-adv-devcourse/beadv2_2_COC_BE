package com.coc.modi.product.application;

import com.coc.modi.product.application.dto.ProductCommand;
import com.coc.modi.product.application.dto.ProductInfo;
import com.coc.modi.product.application.dto.ProductListInfo;
import com.coc.modi.product.application.dto.ProductUpdateCommand;
import com.coc.modi.product.domain.*;
import com.coc.modi.product.infrastructure.ProductImageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    // TODO: 사용자 검증 로직 추가

    private final ProductRepository repository;
    private final ProductImageJpaRepository imageJpaRepository;

    // 3-1. 상품 목록 조회
    @Transactional(readOnly = true)
    public Page<ProductListInfo> getProducts(Pageable pageable) {

        Page<Product> page = repository.findAllByStatus(ProductStatus.ACTIVE, pageable);

        List<Long> thumbnailIds = page.getContent().stream()
                .map(Product::getThumbnailImageId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> thumbnailUrlMap = Collections.emptyMap();
        if (!thumbnailIds.isEmpty()) {
            List<ProductImage> thumbnails = imageJpaRepository.findByIdIn(thumbnailIds);

            thumbnailUrlMap = thumbnails.stream()
                    .collect(Collectors.toMap(
                            ProductImage::getId,
                            ProductImage::getUrl
                    ));
        }

        Map<Long, String> finalThumbnailUrlMap = thumbnailUrlMap;

        return page.map(product ->
                ProductListInfo.of(
                        product,
                        finalThumbnailUrlMap.get(product.getThumbnailImageId())
                )
        );
    }

    // 3-2. 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductInfo getProductDetail(Long productId) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));

        return ProductInfo.from(product);
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
    public ProductInfo createProduct(Long sellerId, ProductCommand command) {

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

        return ProductInfo.from(saved);
    }

    // 3-5. 상품 수정
    @Transactional
    public ProductInfo updateProduct(Long productId, ProductUpdateCommand command) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));

        product.update(command.name(),
                command.description(),
                command.pricePerDay(),
                ProductCategory.from(command.category()));

        //이미지 변경 사항 반영 (null값인 경우 이미지 변동사항 없음)
        if(command.images() != null) {
            product.syncImages(command.images());
        }

        repository.saveAndFlush(product);
        updateThumbnailFromFirstImage(product);

        return ProductInfo.from(product);
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

    // 이미지 동기화


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
    private void changeStatus(Long productId, ProductStatus status) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));
        product.updateStatus(status);
    }
}
