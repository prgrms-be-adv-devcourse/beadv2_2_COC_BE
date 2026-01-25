package com.coc.modi.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.common.NotificationType;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.event.SellerRegistrationRejectedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.notification.application.NotificationApplicationService;
import com.coc.modi.notification.application.SellerApprovalMailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerRegistrationRejectedEventListener {

	private static final String TITLE = "판매자 등록 거부";
	private static final String CONTENT = "판매자 등록이 거부되었습니다. 자세한 내용은 고객센터에 문의해주세요.";

	private final NotificationApplicationService notificationApplicationService;
	private final SellerApprovalMailService sellerApprovalMailService;

	@KafkaListener(
			topics = KafkaTopics.SELLER_REGISTRATION_REJECTED,
			groupId = "support-service-notification",
			containerFactory = "sellerRegistrationRejectedKafkaListenerContainerFactory"
	)
	public void onSellerRejected(SellerRegistrationRejectedEvent event) {

		NotificationEvent notification = NotificationEvent.of(
				event.memberId(),
				NotificationType.SELLER_REJECTED.name(),
				TITLE,
				CONTENT,
				"SELLER_REGISTRATION",
				event.registrationId().toString()
		);

		notificationApplicationService.handle(notification);
		sellerApprovalMailService.sendRejectedMail(event.email());
	}
}
