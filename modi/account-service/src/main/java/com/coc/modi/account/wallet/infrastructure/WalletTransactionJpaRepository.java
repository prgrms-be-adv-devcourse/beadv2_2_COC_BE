package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.domain.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransaction,Long> {

    List<WalletTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId);

	boolean existsByRelatedSettlementIdAndTxType(Long relatedSettlementId, WalletTransactionType txType);
}
