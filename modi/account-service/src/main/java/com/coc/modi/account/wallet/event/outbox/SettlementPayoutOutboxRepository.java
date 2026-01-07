package com.coc.modi.account.wallet.event.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementPayoutOutboxRepository extends JpaRepository<SettlementPayoutOutbox, Long> {
}
