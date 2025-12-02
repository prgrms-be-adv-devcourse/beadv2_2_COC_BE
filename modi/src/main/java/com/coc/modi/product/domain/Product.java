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
}