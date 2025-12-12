package com.coc.modi.notification.domain;

import com.coc.modi.common.NotificationEvent;
import com.coc.modi.common.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long receiverId;
	
	@Enumerated(EnumType.STRING)
	private NotificationType type;
	
	@Column(nullable = false, length = 100)
	private String title;
	
	@Column(nullable = false, length = 500)
	private String content;
	
	private String referenceType;
	private Long referenceId;
	
	private boolean read;
	
	public Notification(Long receiverId,
						NotificationType type,
						String title,
						String content,
						String referenceType,
						Long referenceId) {
		
		this.receiverId = receiverId;
		this.type = type;
		this.title = title;
		this.content = content;
		this.referenceType = referenceType;
		this.referenceId = referenceId;
		this.read = false;
	}
	
	public static Notification fromEvent(NotificationEvent event) {
		
		return new Notification(
				event.getReceiverId(),
				event.getType(),
				event.getTitle(),
				event.getContent(),
				event.getReferenceType(),
				event.getReferenceId()
		);
	}
}
