package com.coc.modi.review.event;

import com.coc.modi.kafka.event.NotificationEvent;

public interface NotificationEventPublisher {
	
	void publish(NotificationEvent event);
}
