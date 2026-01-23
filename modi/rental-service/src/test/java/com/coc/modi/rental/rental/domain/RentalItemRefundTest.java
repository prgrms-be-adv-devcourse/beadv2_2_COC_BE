package com.coc.modi.rental.rental.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RentalItemRefundTest {

	@Test
	void paidItemRefund_marksCanceledAndClearsCharges() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(2);
		BigDecimal unitPrice = new BigDecimal("1000.00");

		RentalItem item = createPaidItem(startDate, endDate, unitPrice);
		Rental rental = Rental.create(1L, BigDecimal.ZERO);
		rental.addItem(item);
		rental.markPaid(LocalDateTime.now());

		BigDecimal refundAmount = item.processRefund();
		rental.recalculateAmountsAndStatus();

		assertThat(item.getStatus()).isEqualTo(RentalItemStatus.CANCELED);
		assertThat(item.getCanceledAt()).isNotNull();
		assertThat(item.getReturnedAt()).isNull();
		assertThat(refundAmount).isEqualByComparingTo(new BigDecimal("3000.00"));
		assertThat(rental.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(rental.getStatus()).isEqualTo(RentalStatus.CANCELED);
	}

	@Test
	void returnedItemRefund_keepsReturnedStatusAndSetsCanceledAt() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(1);
		BigDecimal unitPrice = new BigDecimal("500.00");

		RentalItem item = createPaidItem(startDate, endDate, unitPrice);
		item.startRenting(startDate);
		item.processReturn();

		BigDecimal refundAmount = item.processRefund();

		assertThat(item.getStatus()).isEqualTo(RentalItemStatus.RETURNED);
		assertThat(item.getCanceledAt()).isNotNull();
		assertThat(item.getReturnedAt()).isNotNull();
		assertThat(refundAmount).isEqualByComparingTo(new BigDecimal("1000.00"));
	}

	private RentalItem createPaidItem(LocalDate startDate, LocalDate endDate, BigDecimal unitPrice) {
		RentalItem item = RentalItem.create(1L, 2L, startDate, endDate, unitPrice);
		item.decide(RentalItemStatus.ACCEPTED);
		item.markPaid();
		return item;
	}
}
