package com.coc.modi.product.infrastructure;

import com.coc.modi.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageJpaRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByIdIn(List<Long> ids);
}
