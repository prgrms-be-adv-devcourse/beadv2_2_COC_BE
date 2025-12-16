package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventLog;
import com.coc.modi.rental.rental.domain.RentalEventLogRepository;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RentalEventLogService {
	
	private final RentalEventLogRepository rentalEventLogRepository;
	private final ObjectMapper objectMapper;
	
	
	
	public void logEvent(Rental rental, RentalEventType eventType, Map<String, ?> payload) {
		
		try {
			String payloadJson = objectMapper.writeValueAsString(payload);
			RentalEventLog eventLog = RentalEventLog.create(rental, eventType, payloadJson);
			rentalEventLogRepository.save(eventLog);
		} catch (JsonProcessingException e) {
			log.error("렌탈 이벤트 로그 직렬화 실패. rentalId={}, eventType={}, payload={}",
					rental != null ? rental.getId() : null, eventType, payload, e);
		} catch (Exception e) {
			log.error("렌탈 이벤트 로그 저장 실패. rentalId={}, eventType={}",
					rental != null ? rental.getId() : null, eventType, e);
		}
	}
}
