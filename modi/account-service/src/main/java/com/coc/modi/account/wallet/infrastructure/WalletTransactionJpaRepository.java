package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.domain.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransaction,Long> {

    List<WalletTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId);

	Optional<WalletTransaction> findByTxTypeAndRequestId(WalletTransactionType txType, String requestId);

	boolean existsByRelatedSettlementIdAndTxType(Long relatedSettlementId, WalletTransactionType txType);
}
