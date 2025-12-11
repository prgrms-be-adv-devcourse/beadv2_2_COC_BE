package com.coc.modi.product.product.domain;

import com.coc.modi.product.product.exception.ProductConflictException;
import com.coc.modi.product.product.exception.ProductInvalidInputException;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.coc.modi.common.BaseEntity;

@Getter
@Entity
@Table(name = "product", schema = "public")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "price_per_day", nullable = false, precision = 18, scale = 2)
    private BigDecimal pricePerDay;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Enumerated(STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;

    @Column(name = "thumbnail_image_id")
    private Long thumbnailImageId;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<ProductImage> images = new ArrayList<>();

    protected Product() {}

    private Product(Long sellerId, String name, String description, BigDecimal pricePerDay, ProductStatus status, ProductCategory category) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.status = status;
        this.category = category;
    }

    public static Product create(Long sellerId, String name, String description, BigDecimal pricePerDay, ProductCategory category) {
        if (sellerId == null) {
            throw new ProductInvalidInputException("판매자 ID는 필수입니다.");
        }
        validateProduct(name, description, pricePerDay, category);
        ProductStatus status = ProductStatus.ACTIVE;
        return new Product(sellerId, name, description, pricePerDay, status, category);
    }

    public void update(String name, String description, BigDecimal pricePerDay, ProductCategory category) {
        validateProduct(name, description, pricePerDay, category);
        this.name = name;
        this.description = description;
        this.pricePerDay = pricePerDay;
        this.category = category;
    }

    public void updateStatus(ProductStatus status) {
        if(this.status == ProductStatus.DELETE) {
            throw new ProductConflictException("삭제된 상품은 상태를 변경할 수 없습니다.");
        }
        this.status = status;
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.assignTo(this);
    }

    public void removeImage(ProductImage image) {
        this.images.remove(image);
        image.assignTo(null);
    }

    public void clearImages() {
        List<ProductImage> copy = new ArrayList<>(images);
        for (ProductImage image : copy) {
            removeImage(image);
        }
        this.thumbnailImageId = null;
    }

    public void updateThumbnailImageId(Long thumbnailImageId) {
        this.thumbnailImageId = thumbnailImageId;
    }

    public void syncImages(List<ProductImageSpec> specs) {

        // null 값인 경우 이미지 유지
        if(specs == null) {
            return;
        }

        // 빈 값일 경우 모든 이미지 삭제 후 썸네일 이미지도 초기화
        if(specs.isEmpty()) {
            clearImages();
            this.thumbnailImageId = null;
            return;
        }

        // 현재 이미지들을 id 기준으로 맵핑
        Map<Long, ProductImage> currentById = this.images.stream()
                .filter(img -> img.getId() != null)
                .collect(Collectors.toMap(ProductImage::getId, img -> img));

        // 신규/수정 처리
        List<ProductImage> newImages = new ArrayList<>();

        for (int i = 0; i < specs.size(); i++) {
            ProductImageSpec spec = specs.get(i);
            Integer ordering = spec.ordering() != null ? spec.ordering() : (i + 1);

            if (spec.id() == null) {
                // 새 이미지
                ProductImage created = ProductImage.create(this, spec.url(), ordering);
                newImages.add(created);
            } else {
                // 기존 이미지 수정
                ProductImage existing = currentById.remove(spec.id());
                if (existing == null) {
                    // 없는 id가 왔다: 비즈니스에 따라 exception 또는 새로 추가
                    // 여기서는 예외 던지는 걸 예시로
                    throw new ProductInvalidInputException("유효하지 않은 이미지 ID: " + spec.id());
                }
                existing.update(spec.url(), ordering);
            }
        }

        // 남아 있는 currentById 값들은 요청에서 제거된 이미지 → 삭제 대상
        for (ProductImage toRemove : currentById.values()) {
            removeImage(toRemove);
        }

        // 새 이미지 추가
        for (ProductImage img : newImages) {
            addImage(img);
        }

        // 썸네일 초기화(첫 번째 이미지 기준)
        if (this.images.isEmpty()) {
            this.thumbnailImageId = null;
        } else {
            this.thumbnailImageId = this.images.get(0).getId();
        }
    }

    private static void validateProduct(String name, String description, BigDecimal pricePerDay, ProductCategory category) {
        if (name == null) {
            throw new ProductInvalidInputException("상품명은 필수입니다.");
        }
        if (description == null) {
            throw new ProductInvalidInputException("상품 설명은 필수입니다.");
        }
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductInvalidInputException("대여 가격은 0보다 커야 합니다.");
        }
        if (category == null) {
            throw new ProductInvalidInputException("카테고리는 필수입니다.");
        }
    }
}
