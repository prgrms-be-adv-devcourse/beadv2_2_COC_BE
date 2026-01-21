package com.coc.modi.product.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductOutboxEventRepository extends JpaRepository<ProductOutboxEvent, UUID> {

	@Query(
			value = """
					select *
					from product.product_outbox
					where status = 'PENDING'
					order by created_at
					limit :limit
					for update skip locked
					""",
			nativeQuery = true
	)
	List<ProductOutboxEvent> findPendingForPublish(@Param("limit") int limit);
}
