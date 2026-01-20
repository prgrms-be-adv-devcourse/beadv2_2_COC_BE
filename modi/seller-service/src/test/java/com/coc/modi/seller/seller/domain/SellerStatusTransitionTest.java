package com.coc.modi.seller.seller.domain;

import com.coc.modi.seller.seller.exception.SellerStatusConflictException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SellerStatusTransitionTest {

	@Test
	void create_defaultsToPending() {

		Seller seller = Seller.create(10L, "store-10", "biz-10", "010-0000-0000");

		assertThat(seller.getStatus()).isEqualTo(SellerStatus.PENDING);
	}

	@Test
	void approve_fromPending_setsActive() {

		Seller seller = Seller.create(11L, "store-11", "biz-11", "010-0000-0001");

		seller.approve();

		assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
	}

	@Test
	void reject_fromPending_setsRejected() {

		Seller seller = Seller.create(12L, "store-12", "biz-12", "010-0000-0002");

		seller.reject();

		assertThat(seller.getStatus()).isEqualTo(SellerStatus.REJECTED);
	}

	@Test
	void approve_fromRejected_throwsConflict() {

		Seller seller = Seller.create(13L, "store-13", "biz-13", "010-0000-0003");
		seller.reject();

		assertThatThrownBy(seller::approve)
				.isInstanceOf(SellerStatusConflictException.class);
	}
}
