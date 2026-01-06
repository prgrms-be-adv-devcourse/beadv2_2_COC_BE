package com.coc.modi.account.wallet.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.exception.AccountAlreadyExistsException;
import com.coc.modi.kafka.event.MemberCreatedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCreatedEventListener {

    private final WalletCommandService walletCommandService;

    @KafkaListener(
            topics = KafkaTopics.MEMBER_CREATED,
            groupId = "account-service",
            containerFactory = "memberCreatedKafkaListenerContainerFactory"
    )
    public void onMemberCreated(MemberCreatedEvent event) {

        try {
            walletCommandService.createWalletForMember(event.memberId());
        } catch (AccountAlreadyExistsException ex) {
            log.debug("Wallet already exists for memberId={}", event.memberId());
        }
    }
}
