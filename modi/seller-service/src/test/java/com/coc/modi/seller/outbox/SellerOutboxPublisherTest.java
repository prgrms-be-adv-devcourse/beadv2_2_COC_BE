package com.coc.modi.seller.outbox;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.coc.modi.kafka.event.SellerRegistrationApprovedEvent;
import com.coc.modi.kafka.event.SellerRegistrationRejectedEvent;
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

		SellerRegistrationApprovedEvent payload =
				SellerRegistrationApprovedEvent.of(10L, 110L, "seller10@example.com");
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		String json = mapper.writeValueAsString(payload);
		SellerOutboxEvent event = SellerOutboxEvent.create(
				"SELLER_REGISTRATION",
				10L,
				SellerOutboxEventType.SELLER_REGISTRATION_APPROVED,
				json
		);

		when(outboxEventRepository.findPendingForPublish(10)).thenReturn(List.of(event));
		when(kafkaTemplate.send(eq(KafkaTopics.SELLER_REGISTRATION_APPROVED), eq("10"),
				any(SellerRegistrationApprovedEvent.class)))
				.thenReturn(CompletableFuture.<SendResult<String, Object>>completedFuture(null));

		publisher.publishPendingEvents();

		assertThat(event.getStatus()).isEqualTo(SellerOutboxStatus.SENT);
	}

	@Test
	void publishPendingEvents_handlesRejectedEvent() throws Exception {

		SellerRegistrationRejectedEvent payload =
				SellerRegistrationRejectedEvent.of(20L, 220L, "seller20@example.com");
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		String json = mapper.writeValueAsString(payload);
		SellerOutboxEvent event = SellerOutboxEvent.create(
				"SELLER_REGISTRATION",
				20L,
				SellerOutboxEventType.SELLER_REGISTRATION_REJECTED,
				json
		);

		when(outboxEventRepository.findPendingForPublish(10)).thenReturn(List.of(event));
		when(kafkaTemplate.send(eq(KafkaTopics.SELLER_REGISTRATION_REJECTED), eq("20"),
				any(SellerRegistrationRejectedEvent.class)))
				.thenReturn(CompletableFuture.<SendResult<String, Object>>completedFuture(null));

		publisher.publishPendingEvents();

		assertThat(event.getStatus()).isEqualTo(SellerOutboxStatus.SENT);
	}
}
