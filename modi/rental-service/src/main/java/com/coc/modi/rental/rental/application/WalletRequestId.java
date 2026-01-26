package com.coc.modi.rental.rental.application;

import java.time.LocalDate;

final class WalletRequestId {

	private WalletRequestId() {
	}

	static String payment(Long rentalId) {
		return "rental-payment:" + rentalId;
	}

	static String refund(Long rentalItemId) {
		return "rental-refund:" + rentalItemId;
	}

	static String depositRefund(Long rentalItemId) {
		return "rental-deposit-refund:" + rentalItemId;
	}

	static String extend(Long rentalItemId, LocalDate newEndDate) {
		return "rental-extend:" + rentalItemId + ":" + newEndDate;
	}

	static String paymentCompRefund(Long rentalId, Long rentalItemId) {
		return "rental-payment-comp-refund:" + rentalId + ":" + rentalItemId;
	}

	static String refundCompCharge(Long rentalItemId) {
		return "rental-refund-comp-charge:" + rentalItemId;
	}

	static String depositRefundCompCharge(Long rentalItemId) {
		return "rental-deposit-refund-comp-charge:" + rentalItemId;
	}

	static String extendCompRefund(Long rentalItemId, LocalDate newEndDate) {
		return "rental-extend-comp-refund:" + rentalItemId + ":" + newEndDate;
	}
}
