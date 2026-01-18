package com.coc.modi.seller.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

	@Query("""
			select o from NotificationOutbox o
			where o.status = :status
			  and (o.nextAttemptAt is null or o.nextAttemptAt <= :now)
			order by o.id
			""")
	List<NotificationOutbox> findReadyToPublish(
			@Param("status") NotificationOutboxStatus status,
			@Param("now") Instant now,
			Pageable pageable
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update NotificationOutbox o
			set o.status = :processing
			where o.id = :id
			  and o.status = :pending
			  and (o.nextAttemptAt is null or o.nextAttemptAt <= :now)
			""")
	int claim(
			@Param("id") Long id,
			@Param("pending") NotificationOutboxStatus pending,
			@Param("processing") NotificationOutboxStatus processing,
			@Param("now") Instant now
	);
}
