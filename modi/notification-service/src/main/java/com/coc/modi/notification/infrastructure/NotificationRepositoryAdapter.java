package com.coc.modi.notification.infrastructure;

import org.springframework.stereotype.Repository;

import com.coc.modi.notification.domain.Notification;
import com.coc.modi.notification.domain.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

	private final NotificationJpaRepository notificationJpaRepository;

	@Override
	public Notification save(Notification notification) {
		return notificationJpaRepository.save(notification);
	}
}
