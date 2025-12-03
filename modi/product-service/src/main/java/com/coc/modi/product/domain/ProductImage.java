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

    protected ProductImage() {}

    public ProductImage(Product product, String url, Integer ordering) {
        this.product = product;
        this.url = url;
        this.ordering = ordering;
    }

    public static ProductImage create(Product product, String url, Integer ordering) {
        return new ProductImage(product, url, ordering);
    }

    public void update(String url, Integer ordering) {
        this.url = url;
        this.ordering = ordering;
    }

    void assignTo(Product product) {
        this.product = product;
    }
}
