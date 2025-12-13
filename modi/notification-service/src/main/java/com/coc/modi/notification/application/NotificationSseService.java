package com.coc.modi.notification.application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.coc.modi.notification.domain.Notification;

@Service
public class NotificationSseService {
	
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
	
	public SseEmitter subscribe(Long memberId) {
		
		SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
		emitters.put(memberId, emitter);
		
		emitter.onCompletion(() -> emitters.remove(memberId));
		emitter.onTimeout(() -> emitters.remove(memberId));
		emitter.onError(e -> emitters.remove(memberId));
		
		sendToMember(memberId, "connected");
		
		return emitter;
	}
	
	public void sendNotification(Long memberId, Notification notification) {
		
		SseEmitter emitter = emitters.get(memberId);
		
		if (emitter == null) {
			
			return;
		}
		
		try {
			
			emitter.send(SseEmitter.event()
					.name("notification")
					.id(notification.getId().toString())
					.data(notification));
		} catch (IOException e) {
			
			emitters.remove(memberId);
		}
	}
	
	public void sendToMember(Long memberId, String data) {
		
		SseEmitter emitter = emitters.get(memberId);
		
		if (emitter == null) {
			
			return;
		}
		
		try {
			
			emitter.send(SseEmitter.event().name("heartbeat").data(data));
		} catch (Exception e) {
			
			emitters.remove(memberId);
		}
	}
}
