package com.coc.modi.seller.settlement.infrastructure;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class SellerSettlementLineJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	public boolean insertLineAndAccumulate(Long settlementId,
										   Long sellerId,
										   Long rentalItemId,
										   Long memberId,
										   Long productId,
										   BigDecimal rentalAmount,
										   BigDecimal feeAmount) {

		int updated = jdbcTemplate.update("""
				WITH inserted AS (
					INSERT INTO seller.seller_settlement_line
						(seller_settlement_id, seller_id, rental_item_id, member_id, product_id,
						 rental_amount, fee_amount, status, canceled_at, created_at, updated_at)
					VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', NULL, now(), now())
					ON CONFLICT (seller_settlement_id, rental_item_id) DO NOTHING
					RETURNING rental_amount, fee_amount
				)
				UPDATE seller.seller_settlement
				SET total_rental_amount = total_rental_amount + COALESCE((SELECT SUM(rental_amount) FROM inserted), 0),
					total_fee_amount = total_fee_amount + COALESCE((SELECT SUM(fee_amount) FROM inserted), 0),
					settlement_amount = settlement_amount + COALESCE((SELECT SUM(rental_amount - fee_amount) FROM inserted), 0),
					updated_at = now()
				WHERE id = ?
				  AND EXISTS (SELECT 1 FROM inserted)
				""",
				settlementId,
				sellerId,
				rentalItemId,
				memberId,
				productId,
				rentalAmount,
				feeAmount,
				settlementId);

		return updated > 0;
	}

	public boolean cancelLineAndAdjust(Long settlementId,
									   Long rentalItemId,
									   LocalDateTime canceledAt) {

		int updated = jdbcTemplate.update("""
				WITH canceled AS (
					UPDATE seller.seller_settlement_line
					SET status = 'CANCELED',
						canceled_at = COALESCE(?, now()),
						updated_at = now()
					WHERE seller_settlement_id = ?
					  AND rental_item_id = ?
					  AND status = 'ACTIVE'
					RETURNING rental_amount, fee_amount
				)
				UPDATE seller.seller_settlement
				SET total_rental_amount = total_rental_amount - COALESCE((SELECT SUM(rental_amount) FROM canceled), 0),
					total_fee_amount = total_fee_amount - COALESCE((SELECT SUM(fee_amount) FROM canceled), 0),
					settlement_amount = settlement_amount - COALESCE((SELECT SUM(rental_amount - fee_amount) FROM canceled), 0),
					updated_at = now()
				WHERE id = ?
				  AND EXISTS (SELECT 1 FROM canceled)
				""",
				canceledAt,
				settlementId,
				rentalItemId,
				settlementId);

		return updated > 0;
	}

	public Set<Long> findRentalItemIdsBySellerAndPeriod(Long sellerId, String periodYm) {
		List<Long> rentalItemIds = jdbcTemplate.queryForList("""
				SELECT line.rental_item_id
				FROM seller.seller_settlement_line line
				JOIN seller.seller_settlement settlement
				  ON line.seller_settlement_id = settlement.id
				WHERE settlement.seller_id = ?
				  AND settlement.period_ym = ?
				""", Long.class, sellerId, periodYm);
		return new HashSet<>(rentalItemIds);
	}
}
