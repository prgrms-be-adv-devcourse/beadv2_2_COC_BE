package com.coc.modi.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Page<Product> findAll(Pageable pageable);

    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    Optional<Product> findById(Long id);

    Product save(Product product);

    Product saveAndFlush(Product product);

    List<Product> findByIdIn(List<Long> productIds);
}
