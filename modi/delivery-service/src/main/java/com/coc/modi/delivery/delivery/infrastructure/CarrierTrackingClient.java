package com.coc.modi.delivery.delivery.infrastructure;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.coc.modi.delivery.delivery.infrastructure.dto.TrackerDeliveryResponse;
import com.coc.modi.delivery.delivery.exception.DeliveryTrackingClientException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CarrierTrackingClient {
	
	private final WebClient webClient;
	private final String authorizationValue;
	
	public CarrierTrackingClient(WebClient.Builder builder,
								 @Value("${tracker-delivery.endpoint:https://apis.tracker.delivery/graphql}") String endpoint,
								 @Value("${tracker-delivery.client-id}") String clientId,
								 @Value("${tracker-delivery.client-secret}") String clientSecret) {
		
		this.webClient = builder.baseUrl(endpoint)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
		
		this.authorizationValue = "TRACKQL-API-KEY " + clientId + ":" + clientSecret;
	}
	
	public TrackingResult track(String carrierCode, String trackingNumber) {
		
		carrierCode = carrierCode.trim();
		trackingNumber = trackingNumber.replaceAll("[^0-9A-Za-z]", "");
		
		String query = """
					query track($carrierId: ID!, $trackingNumber: String!) {
						track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
							trackingNumber
							lastEvent {
								description
								status { code name }
							}
						}
					}
				""";
		
		Map<String, Object> body = Map.of("query", query, "variables", Map.of("carrierId", carrierCode, "trackingNumber", trackingNumber));
		
		TrackerDeliveryResponse response;
		try {
			
			response = webClient.post()
					.header(HttpHeaders.AUTHORIZATION, authorizationValue)
					.bodyValue(body)
					.retrieve()
					.bodyToMono(TrackerDeliveryResponse.class)
					.timeout(Duration.ofSeconds(10))
					.block();
		} catch (Exception e) {
			
			log.warn("[배송추적][외부연동] tracker.delivery 호출 실패 (일시적 오류) carrierCode={} trackingNumber={} 오류={}", carrierCode, trackingNumber, e.toString());
			return new TrackingResult("UNKNOWN", "TRANSIENT_NETWORK_ERROR", false);
		}
		
		if (response == null) {
			
			throw new DeliveryTrackingClientException("배송 추적 API 응답이 비어있습니다.");
		}
		
		if (response.errors() != null && !response.errors().isEmpty()) {
			
			String msg = response.errors().get(0).message();
			
			log.warn("[배송추적][외부연동] tracker.delivery 오류 응답 carrierCode={} trackingNumber={} errors={}", carrierCode, trackingNumber, response.errors());
			
			if (isTransientError(msg)) {
				return new TrackingResult("UNKNOWN", "TRANSIENT_ERROR: " + msg, false);
			}
			
			throw new DeliveryTrackingClientException("배송 추적 API 오류: " + msg);
		}
		
		if (response.data() == null || response.data().track() == null) {
			
			log.info("[배송추적][외부연동] 배송 조회 불가 또는 미집하 상태 carrierCode={} trackingNumber={}", carrierCode, trackingNumber);
			
			return new TrackingResult("UNKNOWN", "TRACK_NOT_FOUND_OR_NOT_READY", false);
		}
		
		var lastEvent = response.data().track().lastEvent();
		
		if (lastEvent == null || lastEvent.status() == null || lastEvent.status().code() == null) {
			
			return new TrackingResult("UNKNOWN", null, false);
		}
		
		String code = lastEvent.status().code();
		boolean delivered = "DELIVERED".equals(code);
		String desc = lastEvent.description();
		
		return new TrackingResult(code, desc, delivered);
	}
	
	private boolean isTransientError(String msg) {
		
		if (msg == null) {
			return false;
		}
		String m = msg.toLowerCase();
		return m.contains("try again") || m.contains("internal error") || m.contains("temporar")
				|| m.contains("timeout") || m.contains("rate") || m.contains("too many");
	}
}
