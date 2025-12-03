package com.coc.modi.product.infrastructure;

import com.coc.modi.product.domain.Product;
import com.coc.modi.product.domain.ProductRepository;
import com.coc.modi.product.domain.ProductStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    @Autowired
    private ProductJpaRepository repository;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<Product> findAllByStatus(ProductStatus status, Pageable pageable) {
        return repository.findAllByStatus(status, pageable);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Product save(Product product) {
        return repository.save(product);
    }

    @Override
    public Product saveAndFlush(Product product) {
        return repository.saveAndFlush(product);
    }
}
