package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJpaRepository extends JpaRepository<ProductImage, Long> {

}
