package com.coc.modi.account.wallet.domain;

import java.util.List;

public interface WalletTransactionRepository {

    WalletTransaction save(WalletTransaction tx);

    List<WalletTransaction> findByMemberId(Long memberId);

	boolean existsByRelatedSettlementIdAndTxType(Long relatedSettlementId, WalletTransactionType txType);
}
