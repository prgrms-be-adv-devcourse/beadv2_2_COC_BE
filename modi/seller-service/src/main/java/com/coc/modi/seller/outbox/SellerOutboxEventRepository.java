package com.coc.modi.seller.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerOutboxEventRepository extends JpaRepository<SellerOutboxEvent, UUID> {

	@Query(
			value = """
					select *
					from seller_outbox
					where status = 'PENDING'
					order by created_at
					limit :limit
					for update skip locked
					""",
			nativeQuery = true
	)
	List<SellerOutboxEvent> findPendingForPublish(@Param("limit") int limit);
}
