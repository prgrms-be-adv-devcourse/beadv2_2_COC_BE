package com.coc.modi.product.viewlog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductViewLogRepository extends JpaRepository<ProductViewLog, Long> {
	
	@Query(value = """
			select t.product_id
			from (
			    select distinct on (product_id) product_id, added_to_cart, created_at
			    from product.product_view_log
			    where member_id = :memberId
			      and created_at >= :startAt
			    order by product_id, created_at desc
			) t
			order by t.added_to_cart desc, t.created_at desc
			limit :limit
			""", nativeQuery = true)
	List<Long> findRecentViewedProductIdsWithinPeriodPrioritizingAddedToCart(
			@Param("memberId") Long memberId,
			@Param("startAt") LocalDateTime startAt,
			@Param("limit") int limit);
	
	@Query(value = """
			select t.product_id
			from (
			    select distinct on (product_id) product_id, added_to_cart, created_at
			    from product.product_view_log
			    where member_id = :memberId
			    order by product_id, created_at desc
			) t
			order by t.added_to_cart desc, t.created_at desc
			limit :limit
			""", nativeQuery = true)
	List<Long> findRecentViewedProductIdsPrioritizingAddedToCart(
			@Param("memberId") Long memberId,
			@Param("limit") int limit);
	
	@Modifying
	@Query(value = """
			update product.product_view_log
			set added_to_cart = :addedToCart
			where member_id = :memberId
			  and product_id = :productId
			""", nativeQuery = true)
	int updateAddedToCartByMemberAndProduct(@Param("memberId") Long memberId,
											@Param("productId") Long productId,
											@Param("addedToCart") boolean addedToCart);
}
