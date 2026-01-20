package com.coc.modi.review.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewOutboxEventRepository extends JpaRepository<ReviewOutboxEvent, UUID> {
	
	@Query(
			value = """
					select *
					from review_outbox
					where status = 'PENDING'
					order by created_at
					limit :limit
					for update skip locked
					""",
			nativeQuery = true
	)
	List<ReviewOutboxEvent> findPendingForPublish(@Param("limit") int limit);
}
