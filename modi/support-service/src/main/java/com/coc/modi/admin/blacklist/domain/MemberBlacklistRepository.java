package com.coc.modi.admin.blacklist.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberBlacklistRepository extends JpaRepository<MemberBlacklist, Long> {

	Page<MemberBlacklist> findByStatus(BlacklistStatus status, Pageable pageable);

	List<MemberBlacklist> findByStatus(BlacklistStatus status);

	List<MemberBlacklist> findByMemberIdIn(List<Long> memberIds);

	List<MemberBlacklist> findByStatusAndSuspendedUntilBefore(BlacklistStatus status, LocalDateTime suspendedUntil);
}
