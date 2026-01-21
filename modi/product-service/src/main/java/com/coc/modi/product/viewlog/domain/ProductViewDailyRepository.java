package com.coc.modi.product.viewlog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductViewDailyRepository extends JpaRepository<ProductViewDaily, ProductViewDailyId> {

	@Modifying
	@Query(value = """
			insert into product.product_view_daily (view_date, product_id, view_count, created_at, updated_at)
			values (:viewDate, :productId, 1, now(), now())
			on conflict (view_date, product_id) do update
			set view_count = product_view_daily.view_count + 1,
				updated_at = now()
			""", nativeQuery = true)
	void increment(@Param("viewDate") LocalDate viewDate, @Param("productId") Long productId);

	@Query(value = """
			select pvd.product_id as productId,
			       p.name as productName,
			       sum(pvd.view_count) as viewCount
			from product.product_view_daily pvd
			join product.product p on p.id = pvd.product_id
			where (:startDate is null or pvd.view_date >= :startDate)
			  and (:endDate is null or pvd.view_date <= :endDate)
			group by pvd.product_id, p.name
			order by view_count desc, pvd.product_id
			limit :limit
			""", nativeQuery = true)
	List<PopularProductRow> findPopularProducts(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("limit") int limit);
}
