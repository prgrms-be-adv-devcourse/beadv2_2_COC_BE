package com.coc.modi.rental.rental.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;

class RentalItemStartRentingTest {

	@Test
	void startRenting_allowsOnOrAfterStartDate() {
		LocalDate startDate = LocalDate.now().minusDays(1);
		LocalDate endDate = startDate.plusDays(2);

		RentalItem item = createPaidItem(startDate, endDate);

		item.startRenting(LocalDate.now());

		assertThat(item.getStatus()).isEqualTo(RentalItemStatus.RENTING);
	}

	@Test
	void startRenting_rejectsBeforeStartDate() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = startDate.plusDays(2);

		RentalItem item = createPaidItem(startDate, endDate);

		assertThatThrownBy(() -> item.startRenting(LocalDate.now()))
				.isInstanceOf(RentalStatusInvalidException.class)
				.hasMessageContaining("시작일 이전에는 대여 시작 할 수 없습니다.");
	}

	private RentalItem createPaidItem(LocalDate startDate, LocalDate endDate) {
		RentalItem item = RentalItem.create(1L, 2L, startDate, endDate, new BigDecimal("1000.00"));
		item.decide(RentalItemStatus.ACCEPTED);
		item.markPaid();
		return item;
	}
}
