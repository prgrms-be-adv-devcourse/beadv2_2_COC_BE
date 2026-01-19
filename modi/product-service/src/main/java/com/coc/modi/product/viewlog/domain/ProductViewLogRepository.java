package com.coc.modi.product.viewlog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

	@Modifying
	@Query(value = """
			update product.product_view_log
			set added_to_cart = :addedToCart
			where id = (
			    select id
			    from product.product_view_log
			    where member_id = :memberId
			      and product_id = :productId
			    order by created_at desc
			    limit 1
			)
			""", nativeQuery = true)
	int updateLatestAddedToCart(@Param("memberId") Long memberId,
								@Param("productId") Long productId,
								@Param("addedToCart") boolean addedToCart);
}
