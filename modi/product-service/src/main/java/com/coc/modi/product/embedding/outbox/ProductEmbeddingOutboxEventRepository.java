package com.coc.modi.product.embedding.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductEmbeddingOutboxEventRepository extends JpaRepository<ProductEmbeddingOutboxEvent, UUID> {
	
	@Query(
			value = """
					select *
					from product.product_embedding_outbox
					where status = 'PENDING'
					order by created_at
					limit :limit
					for update skip locked
					""",
			nativeQuery = true
	)
	List<ProductEmbeddingOutboxEvent> findPendingForPublish(@Param("limit") int limit);
	
	@Query(
			value = """
					select p.id
					from product.product p
					left join (
						select aggregate_id, max(processed_at) as last_sent
						from product.product_embedding_outbox
						where event_type = 'PRODUCT_EMBEDDING_EVENT'
						  and status = 'SENT'
						group by aggregate_id
					) o on p.id = o.aggregate_id
					where o.last_sent is null
					   or p.updated_at > o.last_sent
					order by p.updated_at desc
					limit :limit
					""",
			nativeQuery = true
	)
	List<Long> findReindexTargets(@Param("limit") int limit);
}
