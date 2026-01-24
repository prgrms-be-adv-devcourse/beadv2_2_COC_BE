package com.coc.modi.admin.notice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

	@Query("""
			select n from Notice n
			where n.status = :status
			  and (n.displayStartAt is null or n.displayStartAt <= :now)
			  and (n.displayEndAt is null or n.displayEndAt >= :now)
			  and (:keyword is null
				   or lower(n.title) like :keyword
				   or lower(n.content) like :keyword)
			""")
	Page<Notice> findPublishedVisible(@Param("status") NoticeStatus status,
									  @Param("now") LocalDateTime now,
									  @Param("keyword") String keyword,
									  Pageable pageable);

	@Query("""
			select n from Notice n
			where n.id = :noticeId
			  and n.status = :status
			  and (n.displayStartAt is null or n.displayStartAt <= :now)
			  and (n.displayEndAt is null or n.displayEndAt >= :now)
			""")
	Optional<Notice> findPublishedVisibleById(@Param("noticeId") Long noticeId,
											  @Param("status") NoticeStatus status,
											  @Param("now") LocalDateTime now);

	@Query("""
			select n from Notice n
			where (:status is null or n.status = :status)
			  and (:keyword is null
				   or lower(n.title) like :keyword
				   or lower(n.content) like :keyword)
			""")
	Page<Notice> findAdminNotices(@Param("status") NoticeStatus status,
								  @Param("keyword") String keyword,
								  Pageable pageable);
}
