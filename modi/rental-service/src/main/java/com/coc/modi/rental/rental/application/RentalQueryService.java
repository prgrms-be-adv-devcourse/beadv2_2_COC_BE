package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.rental.rental.application.dto.RentalItemResponse;
import com.coc.modi.rental.rental.application.dto.RentalResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.exception.RentalAccessDeniedException;
import com.coc.modi.rental.rental.exception.RentalException;
import com.coc.modi.rental.rental.exception.RentalNotFoundException;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalInternalSearchCondition;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemInfo;
import com.coc.modi.rental.rental.infrastructure.client.dto.RentalItemInfoListResponse;
import com.coc.modi.rental.rental.infrastructure.client.dto.UnavailableProductsRequest;
import com.coc.modi.rental.rental.infrastructure.client.dto.UnavailableProductsResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalQueryService {
	
	private final RentalRepository rentalRepository;
	private final RentalQueryRepository rentalQueryRepository;
	
	public RentalResponse getRentalDetails(Long rentalId, Long memberId) {
		
		Rental rental = rentalRepository.findById(rentalId)
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
			
			return new RentalItemInfoListResponse(rentalItemInfoList, rentalItems.getTotalPages(), rentalItems.getTotalPages());
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
		
		return new RentalItemInfoListResponse(rentalItemInfoList, rentalItems.getTotalPages(), rentalItems.getTotalPages());
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
				rental != null ? rental.getPaidAt() : null
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
}
