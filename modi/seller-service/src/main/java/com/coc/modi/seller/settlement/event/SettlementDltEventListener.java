package com.coc.modi.seller.settlement.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SettlementDltEventListener {

	@KafkaListener(
			topics = {
					KafkaTopics.RENTAL_RETURNED_EVENTS + ".DLT",
					KafkaTopics.ACCOUNT_SETTLEMENT_PAYOUT_COMPLETED + ".DLT",
					KafkaTopics.ACCOUNT_SETTLEMENT_PAYOUT_FAILED + ".DLT"
			},
			groupId = "seller-service-dlt",
			containerFactory = "dltKafkaListenerContainerFactory"
	)
	public void onDltMessage(ConsumerRecord<String, String> record,
							 @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
							 @Header(value = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) String exceptionClass,
							 @Header(value = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic) {

		log.error("DLT message received. topic={}, originalTopic={}, key={}, offset={}, exceptionClass={}, exceptionMessage={}, payload={}",
				record.topic(),
				originalTopic,
				record.key(),
				record.offset(),
				exceptionClass,
				exceptionMessage,
				record.value()
		);
	}
}
