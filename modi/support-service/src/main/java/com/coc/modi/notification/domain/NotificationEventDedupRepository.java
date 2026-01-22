package com.coc.modi.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventDedupRepository extends JpaRepository<NotificationEventDedup, Long> {
}
