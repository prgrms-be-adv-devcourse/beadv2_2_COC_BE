package com.coc.modi.product.product.infrastructure;

import java.util.List;

import com.coc.modi.product.product.domain.ProductImage;
import com.coc.modi.product.product.infrastructure.dto.ProductImageUrlDto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageJpaRepository extends JpaRepository<ProductImage, Long> {
	
	@Query("""
        select new com.coc.modi.product.product.infrastructure.dto.ProductImageUrlDto(
            pi.id,
            pi.url
        )
        from ProductImage pi
        where pi.id in :ids
    """)
	List<ProductImageUrlDto> findUrlDtoByIds(@Param("ids") List<Long> ids);

}
