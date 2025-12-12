package com.coc.modi.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coc.modi.notification.domain.Notification;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

}
