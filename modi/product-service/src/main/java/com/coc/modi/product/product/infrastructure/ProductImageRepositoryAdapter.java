package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.domain.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductImageRepositoryAdapter implements ProductImageRepository {

    @Autowired
    private ProductImageJpaRepository repository;

    @Override
    public Optional<ProductImage> findById(Long thumbnailId) {

        return repository.findById(thumbnailId);
    }
}
