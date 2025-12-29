package com.coc.modi.member.member.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.coc.modi.kafka.event.MemberCreatedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberCreatedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(MemberCreatedEvent event) {

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(MemberCreatedEvent event) {

        kafkaTemplate.send(KafkaTopics.MEMBER_CREATED, event.memberId().toString(), event);
    }
}
