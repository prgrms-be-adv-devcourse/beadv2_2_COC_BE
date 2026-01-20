package com.coc.modi.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.notification.application.NotificationApplicationService;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventListener {
	
	private final NotificationApplicationService notificationApplicationService;
	
	@KafkaListener(
			topics = KafkaTopics.NOTIFICATION_EVENTS,
			groupId = "support-service",
			containerFactory = "notificationKafkaListenerContainerFactory"
	)
	public void onNotificationEvent(NotificationEvent event) {
		
		notificationApplicationService.handle(event);
	}
	
}
