package com.coc.modi.account.wallet.event.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface SettlementPayoutOutboxRepository extends JpaRepository<SettlementPayoutOutbox, Long> {
	
	@Query("""
			select o from SettlementPayoutOutbox o
			where o.status = :status
			  and (o.nextAttemptAt is null or o.nextAttemptAt <= :now)
			order by o.id
			""")
	List<SettlementPayoutOutbox> findReadyToPublish(
			@Param("status") SettlementPayoutOutboxStatus status,
			@Param("now") Instant now,
			Pageable pageable
	);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update SettlementPayoutOutbox o
			set o.status = :processing
			where o.id = :id
			  and o.status = :pending
			  and (o.nextAttemptAt is null or o.nextAttemptAt <= :now)
			""")
	int claim(
			@Param("id") Long id,
			@Param("pending") SettlementPayoutOutboxStatus pending,
			@Param("processing") SettlementPayoutOutboxStatus processing,
			@Param("now") Instant now
	);
}
