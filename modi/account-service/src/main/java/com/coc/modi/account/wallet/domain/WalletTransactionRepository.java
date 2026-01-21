package com.coc.modi.account.wallet.domain;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository {

    WalletTransaction save(WalletTransaction tx);

    List<WalletTransaction> findByMemberId(Long memberId);

	Optional<WalletTransaction> findByTxTypeAndRequestId(WalletTransactionType txType, String requestId);

	boolean existsByRelatedSettlementIdAndTxType(Long relatedSettlementId, WalletTransactionType txType);
}
