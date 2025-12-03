package com.coc.modi.product.domain;

import jakarta.persistence.*;
import lombok.Getter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Entity
@Table(name = "product_image", schema = "public")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private Integer ordering;

    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail = false;

    static ProductImage createProductImage(String url, Integer ordering, Boolean isThumbnail) {
        ProductImage image = new ProductImage();
        image.url = url;
        image.ordering = ordering;
        image.isThumbnail = isThumbnail;
        return image;
    }

    void assignTo(Product product) {
        this.product = product;
    }
}