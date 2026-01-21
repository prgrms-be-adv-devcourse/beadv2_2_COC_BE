package com.coc.modi.review.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.kafka.event.RentalReturnedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.review.cache.ReturnedRentalCache;
import com.coc.modi.review.cache.ReturnedRentalItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalReturnedEventListener {

	private final ReturnedRentalCache returnedRentalCache;

	@KafkaListener(
			topics = KafkaTopics.RENTAL_RETURNED_EVENTS,
			groupId = "review-service",
			containerFactory = "rentalReturnedKafkaListenerContainerFactory"
	)
	public void onRentalReturned(RentalReturnedEvent event) {
		ReturnedRentalItem cached = new ReturnedRentalItem(
				event.rentalItemId(),
				event.memberId(),
				event.sellerId(),
				event.productId(),
				event.status(),
				event.returnedAt()
		);
		returnedRentalCache.save(cached);
		log.info("Cached returned rental item. rentalItemId={}, memberId={}, sellerId={}",
				event.rentalItemId(), event.memberId(), event.sellerId());
	}
}
