package com.coc.modi.seller.settlement.application;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.domain.SellerStatus;
import com.coc.modi.seller.seller.infrastructure.client.rental.RentalFeignClient;
import com.coc.modi.seller.seller.infrastructure.client.rental.dto.RentalItemInfo;
import com.coc.modi.seller.seller.infrastructure.client.rental.dto.RentalListResponse;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementLineJdbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementFinalCheckerService {

	private static final String STATUS_RETURNED = "RETURNED";
	private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	private final SellerRepository sellerRepository;
	private final RentalFeignClient rentalFeignClient;
	private final SettlementAggregationService settlementAggregationService;
	private final SellerSettlementLineJdbcRepository settlementLineJdbcRepository;

	public void runFinalCheck(YearMonth targetMonth, int pageSize) {
		if (targetMonth == null) {
			return;
		}

		String periodYm = targetMonth.format(PERIOD_FORMATTER);
		LocalDate startDate = targetMonth.atDay(1);
		LocalDate endDate = targetMonth.atEndOfMonth();
		List<Long> sellerIds = loadActiveSellerIds();
		int resolvedPageSize = pageSize > 0 ? pageSize : 200;

		for (Long sellerId : sellerIds) {
			if (sellerId == null) {
				continue;
			}
			Set<Long> existingRentalItemIds = new HashSet<>(
					settlementLineJdbcRepository.findRentalItemIdsBySellerAndPeriod(sellerId, periodYm)
			);
			List<Long> missingRentalItemIds = new ArrayList<>();
			int page = 0;
			while (true) {
				RentalListResponse response = rentalFeignClient.getRentals(
						sellerId,
						null,
						STATUS_RETURNED,
						startDate.toString(),
						endDate.toString(),
						page,
						resolvedPageSize
				);
				List<RentalItemInfo> rentals = response != null ? response.content() : List.of();
				if (rentals == null || rentals.isEmpty()) {
					break;
				}
				for (RentalItemInfo rental : rentals) {
					if (rental == null || rental.rentalItemId() == null) {
						continue;
					}
					if (existingRentalItemIds.contains(rental.rentalItemId())) {
						continue;
					}
					settlementAggregationService.aggregateLine(
							null,
							sellerId,
							periodYm,
							rental.rentalItemId(),
							rental.memberId(),
							rental.productId(),
							rental.totalAmount()
					);
					missingRentalItemIds.add(rental.rentalItemId());
				}
				Integer totalPages = response != null ? response.totalPages() : null;
				if (totalPages == null || page + 1 >= totalPages) {
					break;
				}
				page++;
			}

			if (!missingRentalItemIds.isEmpty()) {
				log.warn("Settlement final check missing lines. sellerId={}, periodYm={}, missingCount={}, missingRentalItemIds={}",
						sellerId,
						periodYm,
						missingRentalItemIds.size(),
						missingRentalItemIds
				);
			} else {
				log.info("Settlement final check completed with no missing lines. sellerId={}, periodYm={}",
						sellerId,
						periodYm
				);
			}
		}
	}

	private List<Long> loadActiveSellerIds() {
		List<Seller> activeSellers = sellerRepository.findByStatus(SellerStatus.ACTIVE);
		List<Long> ids = new ArrayList<>();
		for (Seller seller : activeSellers) {
			if (seller != null && seller.getId() != null) {
				ids.add(seller.getId());
			}
		}
		return ids;
	}
}
