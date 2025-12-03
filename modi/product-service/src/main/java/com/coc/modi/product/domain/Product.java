package com.coc.modi.product.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Entity
@Table(name = "product", schema = "public")
public class Product {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String name;

    @Lob
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
            throw new IllegalArgumentException("sellerId cannot be null");
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
            throw new IllegalArgumentException("PRODUCT STATUS CANNOT BE DELETE");
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
    }

    private static void validateProduct(String name, String description, BigDecimal pricePerDay, ProductCategory category) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (description == null) {
            throw new IllegalArgumentException("description cannot be null");
        }
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("price per day must be greater than 0");
        }
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
    }
}