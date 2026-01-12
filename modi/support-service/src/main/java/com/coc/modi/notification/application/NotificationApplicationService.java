package com.coc.modi.notification.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.coc.modi.notification.domain.Notification;
import com.coc.modi.notification.domain.NotificationRepository;
import com.coc.modi.kafka.event.NotificationEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationApplicationService {
	
	private final NotificationRepository notificationRepository;
	private final NotificationSseService notificationSseService;
	
	@Transactional
	public void handle(NotificationEvent event) {
		
		Notification notification = Notification.fromEvent(event);
		Notification saved = notificationRepository.save(notification);
		notificationSseService.sendNotification(event.receiverId(), saved);
	}
}
