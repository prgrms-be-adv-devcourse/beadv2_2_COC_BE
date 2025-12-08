package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransaction,Long> {

}
