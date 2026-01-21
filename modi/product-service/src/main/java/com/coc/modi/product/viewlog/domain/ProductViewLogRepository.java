package com.coc.modi.product.viewlog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductViewLogRepository extends JpaRepository<ProductViewLog, Long> {

	@Query(value = """
			select product_id
			from (
			    select distinct on (product_id) product_id, created_at
			    from product.product_view_log
			    where member_id = :memberId
			    order by product_id, created_at desc
			) t
			order by t.created_at desc
			limit :limit
			""", nativeQuery = true)
	List<Long> findRecentViewedProductIds(@Param("memberId") Long memberId, @Param("limit") int limit);
}
