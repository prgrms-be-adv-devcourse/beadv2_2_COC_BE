package com.coc.modi.product.infrastructure;

import com.coc.modi.product.domain.ProductImage;
import com.coc.modi.product.domain.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductImageRepositoryAdapter implements ProductImageRepository {

    @Autowired
    private ProductImageJpaRepository repository;

    @Override
    public List<ProductImage> findByIdIn(List<Long> ids) {
        return repository.findByIdIn(ids);
    }
}
