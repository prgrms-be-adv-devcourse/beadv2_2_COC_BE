package com.coc.modi.seller.outbox;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.coc.modi.kafka.event.SellerApprovedEvent;
import com.coc.modi.kafka.event.SellerRejectedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerOutboxPublisherTest {

	@Mock
	private SellerOutboxEventRepository outboxEventRepository;

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	private SellerOutboxPublisher publisher;

	@BeforeEach
	void setUp() {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		publisher = new SellerOutboxPublisher(outboxEventRepository, kafkaTemplate, objectMapper);
		ReflectionTestUtils.setField(publisher, "batchSize", 10);
		ReflectionTestUtils.setField(publisher, "maxRetries", 3);
	}

	@Test
	void publishPendingEvents_marksSentOnSuccess() throws Exception {

		SellerApprovedEvent payload = SellerApprovedEvent.of(1L, 10L);
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		String json = mapper.writeValueAsString(payload);
		SellerOutboxEvent event = SellerOutboxEvent.create(
				"SELLER",
				1L,
				SellerOutboxEventType.SELLER_APPROVED,
				json
		);

		when(outboxEventRepository.findPendingForPublish(10)).thenReturn(List.of(event));
		when(kafkaTemplate.send(eq(KafkaTopics.SELLER_APPROVED), eq("1"), any(SellerApprovedEvent.class)))
				.thenReturn(CompletableFuture.<SendResult<String, Object>>completedFuture(null));

		publisher.publishPendingEvents();

		assertThat(event.getStatus()).isEqualTo(SellerOutboxStatus.SENT);
	}

	@Test
	void publishPendingEvents_handlesRejectedEvent() throws Exception {

		SellerRejectedEvent payload = SellerRejectedEvent.of(2L, 20L);
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		String json = mapper.writeValueAsString(payload);
		SellerOutboxEvent event = SellerOutboxEvent.create(
				"SELLER",
				2L,
				SellerOutboxEventType.SELLER_REJECTED,
				json
		);

		when(outboxEventRepository.findPendingForPublish(10)).thenReturn(List.of(event));
		when(kafkaTemplate.send(eq(KafkaTopics.SELLER_REJECTED), eq("2"), any(SellerRejectedEvent.class)))
				.thenReturn(CompletableFuture.<SendResult<String, Object>>completedFuture(null));

		publisher.publishPendingEvents();

		assertThat(event.getStatus()).isEqualTo(SellerOutboxStatus.SENT);
	}
}
