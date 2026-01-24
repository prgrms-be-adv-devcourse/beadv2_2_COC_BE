package com.coc.modi.delivery.delivery.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coc.modi.delivery.delivery.domain.Delivery;

public interface DeliveryJpaRepository extends JpaRepository<Delivery, Long> {

	Optional<Delivery> findByRentalItemId(Long rentalItemId);
	
	@Query(value = """
			SELECT *
				FROM support.delivery d
			WHERE d.status not in('DELIVERED', 'CANCELLED')
				AND (d.last_tracked_at is null
					OR d.last_tracked_at < (now() - make_interval(mins => :intervalMinutes)))
			ORDER BY d.last_tracked_at nulls first, d.id
			FOR update skip locked
			LIMIT :limit
			""", nativeQuery = true)
	List<Delivery> findTargetsForTrackingWithSkipLocked(
			@Param("intervalMinutes") int intervalMinutes,
			@Param("limit") int limit
	);
}
