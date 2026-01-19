package com.coc.modi.review.event;

import com.coc.modi.kafka.event.NotificationEvent;

public interface NotificationEventPublisher {
	
	void publish(Long reviewId, NotificationEvent event);
}
