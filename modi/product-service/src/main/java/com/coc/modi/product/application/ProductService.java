package com.coc.modi.product.application;

import com.coc.modi.product.application.dto.ProductCommand;
import com.coc.modi.product.application.dto.ProductInfo;
import com.coc.modi.product.application.dto.ProductListInfo;
import com.coc.modi.product.domain.*;
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

    // 3-1. 상품 목록 조회
    @Transactional(readOnly = true)
    public Page<ProductListInfo> getProducts(Pageable pageable) {

        Page<Product> page = repository.findAllByStatus(ProductStatus.ACTIVE, pageable);

        return page.map(ProductListInfo::from);
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

        Product saved = repository.save(product);

        return ProductInfo.from(saved);
    }

    // 3-5. 상품 수정
    @Transactional
    public ProductInfo updateProduct(Long productId, ProductCommand command) {

        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));

        product.update(command.name(),
                command.description(),
                command.pricePerDay(),
                ProductCategory.from(command.category()));

        //이미지 변경 사항 반영
        product.clearImages();
        addImages(product, command.imageUrls());

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

    // 상품에 이미지 추가하기
    private void addImages(Product product, List<String> imageUrls) {
        if(imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (int i=0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            boolean isThumbnail = (i==0);
            int ordering = i + 1;

            ProductImage image = ProductImage.create(product, url, ordering, isThumbnail);
            product.addImage(image);
        }
    }

    // 상품 상태 변경
    private void changeStatus(Long productId, ProductStatus status) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT NOT FOUND: " + productId));
        product.updateStatus(status);
    }
}
