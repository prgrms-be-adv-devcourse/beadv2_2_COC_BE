package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.rental.rental.application.dto.RentalItemResponse;
import com.coc.modi.rental.rental.application.dto.RentalResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemRepository;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.exception.RentalAccessDeniedException;
import com.coc.modi.rental.rental.exception.RentalException;
import com.coc.modi.rental.rental.exception.RentalItemNotFoundException;
import com.coc.modi.rental.rental.exception.RentalNotFoundException;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalInternalSearchCondition;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemInfo;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemInfoListResponse;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemSellerResponse;
import com.coc.modi.rental.rental.infrastructure.client.dto.UnavailableProductsRequest;
import com.coc.modi.rental.rental.infrastructure.client.dto.UnavailableProductsResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalQueryService {
	
	private final RentalRepository rentalRepository;
	private final RentalQueryRepository rentalQueryRepository;
	private final RentalItemRepository rentalItemRepository;

	public RentalResponse getRentalDetails(Long rentalId, Long memberId) {
		
		Rental rental = rentalRepository.findByIdWithItems(rentalId)
				.orElseThrow(() -> new RentalNotFoundException(rentalId));
		
		if (!rental.getMemberId().equals(memberId)) {
			
			throw RentalAccessDeniedException.memberMismatch(rentalId, memberId);
		}
		
		List<RentalItem> rentalItemList = rental.getItems();
		
		List<RentalItemResponse> rentalItemResponseList = rentalItemList.stream()
				.map(RentalItemResponse::from)
				.toList();
		
		return RentalResponse.create(rental, rentalItemResponseList);
	}
	
	public List<RentalResponse> searchRentals(LocalDate startDate,
											  LocalDate endDate,
											  RentalStatus status,
											  Long memberId) {
		
		List<Rental> rentals = rentalQueryRepository.search(startDate, endDate, status, memberId);
		
		
		
		return rentals.stream().map(rental -> {
			
			List<RentalItemResponse> itemResponses = rental.getItems().stream().map(RentalItemResponse::from).toList();
			
			return RentalResponse.create(rental, itemResponses);
		}).toList();
	}
	
	public RentalItemInfoListResponse getRentalItemList(RentalInternalSearchCondition condition, Pageable pageable) {
		
		validateCondition(condition);
		
		if (condition.productId() != null) {
			
			Page<RentalItem> rentalItems = rentalQueryRepository.findRentalItemsBySellerAndProduct(
					condition.sellerId(),
					condition.productId(),
					condition.status(),
					condition.startDate(),
					condition.endDate(),
					pageable);
			
			List<RentalItemInfo> rentalItemInfoList = rentalItems.getContent()
					.stream().map(this::toRentalItemInfo).toList();
			
			return new RentalItemInfoListResponse(rentalItemInfoList, rentalItems.getTotalElements(), rentalItems.getTotalPages());
		}
		
		if (condition.status() != RentalItemStatus.RETURNED) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "productId 없이 조회할 때는 status는 RETURNED 이어야 합니다.");
		}
		
		Page<RentalItem> rentalItems = rentalQueryRepository.findCompletedRentalItemsBySeller(
				condition.sellerId(),
				condition.startDate(),
				condition.endDate(),
				pageable);
		
		List<RentalItemInfo> rentalItemInfoList = rentalItems.stream().map(this::toRentalItemInfo).toList();
		
		return new RentalItemInfoListResponse(rentalItemInfoList, rentalItems.getTotalElements(), rentalItems.getTotalPages());
	}

	@Transactional(readOnly = true)
	public RentalItemSellerResponse getRentalItemSellerInfo(Long rentalItemId) {

		if (rentalItemId == null) {
			throw new RentalException(ErrorCode.INVALID_INPUT, "rentalItemId는 필수입니다.");
		}

		RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
				.orElseThrow(() -> new RentalItemNotFoundException(rentalItemId));

		Long memberId = rentalItem.getRental() != null ? rentalItem.getRental().getMemberId() : null;

		return new RentalItemSellerResponse(rentalItem.getId(), rentalItem.getSellerId(), memberId);
	}

	public RentalItemInfo getRentalItemInfo(Long rentalItemId) {
		RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
				.orElseThrow(() -> new RentalItemNotFoundException(rentalItemId));

		return toRentalItemInfo(rentalItem);
	}

	private void validateCondition(RentalInternalSearchCondition condition) {
		
		if (condition == null) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "검색 조건은 필수입니다.");
		}
		
		if (condition.sellerId() == null) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "sellerId는 필수입니다.");
		}
		
		if (condition.status() == null) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "status는 필수입니다.");
		}
		
		if (condition.startDate() == null || condition.endDate() == null) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "startDate와 endDate는 필수입니다.");
		}
		
		if (condition.startDate().isAfter(condition.endDate())) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "startDate는 endDate보다 이후일 수 없습니다.");
		}
	}
	
	private RentalItemInfo toRentalItemInfo(RentalItem rentalItem) {
		
		Rental rental = rentalItem.getRental();
		
		return new RentalItemInfo(
				rentalItem.getId(),
				rentalItem.getProductId(),
				rental != null ? rental.getMemberId() : null,
				rentalItem.getSellerId(),
				rentalItem.getStatus().name(),
				rentalItem.calculateRentalAmount(),
				rentalItem.getStartDate(),
				rentalItem.getEndDate(),
				rental != null ? rental.getPaidAt() : null,
				rentalItem.getReturnedAt()
		);
	}
	
	public UnavailableProductsResponse getUnavailableProducts(UnavailableProductsRequest unavailableProductsRequest) {
		
		unavailableProductsRequest.vaildate();
		
		List<Long> unavailableProductIds = rentalQueryRepository.findUnavailableProductIds(
				unavailableProductsRequest.startDate(),
				unavailableProductsRequest.endDate(),
				unavailableProductsRequest.productIds()
		);
		
		return new UnavailableProductsResponse(unavailableProductIds);
	}
	
	public List<LocalDate> findUnavailableDates(Long productId, YearMonth ym) {

		if (productId == null) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "productId는 필수입니다.");
		}
		
		if (ym == null) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "ym은 필수입니다.");
		}
		
		LocalDate startDate = ym.atDay(1);
		LocalDate endDate = ym.atEndOfMonth();
		
		List<RentalItem> rentalItems = rentalQueryRepository.findUnavailableRentalItems(productId, startDate, endDate);
		
		if (rentalItems.isEmpty()) {
			
			return List.of();
		}
		
		return rentalItems.stream()
				.flatMap(item -> {
					
					LocalDate from = item.getStartDate().isBefore(startDate) ? startDate : item.getStartDate();
					LocalDate to = item.getEndDate().isAfter(endDate) ? endDate : item.getEndDate();
					
					return from.datesUntil(to.plusDays(1));
				})
				.collect(Collectors.toCollection(TreeSet::new))
				.stream()
				.toList();
	}
}
