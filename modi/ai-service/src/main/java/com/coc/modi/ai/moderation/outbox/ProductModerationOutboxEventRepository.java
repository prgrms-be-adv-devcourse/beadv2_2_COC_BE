package com.coc.modi.ai.moderation.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductModerationOutboxEventRepository extends JpaRepository<ProductModerationOutboxEvent, UUID> {

	@Query(
			value = """
					select *
					from product_moderation_outbox
					where status = 'PENDING'
					order by created_at
					limit :limit
					for update skip locked
					""",
			nativeQuery = true
	)
	List<ProductModerationOutboxEvent> findPendingForPublish(@Param("limit") int limit);
}
