package com.coc.modi.member.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberOutboxEventRepository extends JpaRepository<MemberOutboxEvent, UUID> {
	
	@Query(
			value = """
					select *
					from member_outbox
					where status = 'PENDING'
					order by created_at
					limit :limit
					for update skip locked
					""",
			nativeQuery = true
	)
	List<MemberOutboxEvent> findPendingForPublish(@Param("limit") int limit);
}
