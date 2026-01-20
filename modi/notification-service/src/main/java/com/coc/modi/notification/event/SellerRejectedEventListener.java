package com.coc.modi.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.common.NotificationType;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.event.SellerRejectedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.notification.application.NotificationApplicationService;
import com.coc.modi.notification.application.SellerApprovalMailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerRejectedEventListener {

	private static final String TITLE = "판매자 등록 거부";
	private static final String CONTENT = "판매자 등록이 거부되었습니다. 자세한 내용은 고객센터에 문의해주세요.";

	private final NotificationApplicationService notificationApplicationService;
	private final SellerApprovalMailService sellerApprovalMailService;

	@KafkaListener(
			topics = KafkaTopics.SELLER_REJECTED,
			groupId = "notification-service",
			containerFactory = "notificationKafkaListenerContainerFactory"
	)
	public void onSellerRejected(SellerRejectedEvent event) {

		NotificationEvent notification = NotificationEvent.of(
				event.memberId(),
				NotificationType.SELLER_REJECTED.name(),
				TITLE,
				CONTENT,
				"SELLER",
				event.sellerId().toString()
		);

		notificationApplicationService.handle(notification);
		sellerApprovalMailService.sendRejectedMail(event.email());
	}
}
