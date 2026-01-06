package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record MemberCreatedEvent(
        String eventId,
        Instant occurredAt,
        Long memberId,
        String email
) {

    public static MemberCreatedEvent of(Long memberId, String email) {

        return new MemberCreatedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                memberId,
                email
        );
    }
}
