package com.coc.modi.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.common.NotificationEvent;
import com.coc.modi.notification.application.NotificationApplicationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventListener {
	
	private final NotificationApplicationService notificationApplicationService;
	
	@KafkaListener(
			topics = "notification-events",
			groupId = "notification-service",
			containerFactory = "notificationKafkaListenerContainerFactory"
	)
	public void onNotificationEvent(NotificationEvent event) {
		
		notificationApplicationService.handle(event);
	}
	
}
