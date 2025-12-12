package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    List<Product> findByIdIn(Collection<Long> ids);
}
