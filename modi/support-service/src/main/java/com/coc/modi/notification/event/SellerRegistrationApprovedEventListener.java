package com.coc.modi.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.common.NotificationType;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.event.SellerRegistrationApprovedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.notification.application.NotificationApplicationService;
import com.coc.modi.notification.application.SellerApprovalMailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerRegistrationApprovedEventListener {

	private static final String TITLE = "판매자 등록 승인";
	private static final String CONTENT = "판매자 등록이 승인되었습니다.";

	private final NotificationApplicationService notificationApplicationService;
	private final SellerApprovalMailService sellerApprovalMailService;

	@KafkaListener(
			topics = KafkaTopics.SELLER_REGISTRATION_APPROVED,
			groupId = "support-service-notification",
			containerFactory = "sellerRegistrationApprovedKafkaListenerContainerFactory"
	)
	public void onSellerApproved(SellerRegistrationApprovedEvent event) {

		NotificationEvent notification = NotificationEvent.of(
				event.memberId(),
				NotificationType.SELLER_APPROVED.name(),
				TITLE,
				CONTENT,
				"SELLER_REGISTRATION",
				event.registrationId().toString()
		);

		notificationApplicationService.handle(notification);
		sellerApprovalMailService.sendApprovedMail(event.email());
	}
}
