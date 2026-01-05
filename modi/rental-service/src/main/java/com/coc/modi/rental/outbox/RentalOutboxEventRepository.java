package com.coc.modi.rental.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalOutboxEventRepository extends JpaRepository<RentalOutboxEvent, UUID> {
	
	@Query(
			value = """
					select *
					from rental_outbox
					where status = 'PENDING'
					order by created_at
					limit :limit
					for update skip locked
					""",
			nativeQuery = true
	)
	List<RentalOutboxEvent> findPendingForPublish(@Param("limit") int limit);
}
