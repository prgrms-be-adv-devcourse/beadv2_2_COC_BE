package com.coc.modi.product.searchlog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductSearchLogRepository extends JpaRepository<ProductSearchLog, Long> {

	List<ProductSearchLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

	List<ProductSearchLog> findByCreatedAtGreaterThanEqual(LocalDateTime start);

	List<ProductSearchLog> findByCreatedAtLessThanEqual(LocalDateTime end);

	@Query(value = """
			select keyword
			from (
			    select distinct on (keyword) keyword, created_at
			    from product.product_search_log
			    where member_id = :memberId
			    order by keyword, created_at desc
			) t
			order by t.created_at desc
			limit :limit
			""", nativeQuery = true)
	List<String> findRecentKeywords(@Param("memberId") Long memberId, @Param("limit") int limit);
}
