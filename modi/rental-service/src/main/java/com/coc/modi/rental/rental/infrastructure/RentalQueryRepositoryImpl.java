package com.coc.modi.rental.rental.infrastructure;

import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.domain.QRental;
import com.coc.modi.rental.rental.domain.QRentalItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RentalQueryRepositoryImpl implements RentalQueryRepository {
	
	private final JPAQueryFactory queryFactory;
	private static final List<RentalItemStatus> UNAVAILABLE_STATUSES = List.of(
			RentalItemStatus.REQUESTED,
			RentalItemStatus.ACCEPTED,
			RentalItemStatus.RENTING,
			RentalItemStatus.PAID
	);
	
	@Override
	public List<Rental> search(LocalDate startDate, LocalDate endDate, RentalStatus rentalStatus, Long memberId) {
		
		QRental rental = QRental.rental;
		QRentalItem rentalItem = QRentalItem.rentalItem;
		
		BooleanBuilder builder = new BooleanBuilder();
		
		builder.and(rental.memberId.eq(memberId));
		
		if (startDate != null) {
			builder.and(rentalItem.startDate.goe(startDate));
		}
		
		if (endDate != null) {
			builder.and(rentalItem.endDate.loe(endDate));
		}
		
		if (rentalStatus != null) {
			builder.and(rental.status.eq(rentalStatus));
		}
		
		return queryFactory
				.selectFrom(rental)
				.distinct()
				.leftJoin(rental.items, rentalItem).fetchJoin()
				.where(builder)
				.fetch();
	}
	
	@Override
	public Page<RentalItem> findRentalItemsBySellerAndProduct(Long sellerId,
															  Long productId,
															  RentalItemStatus status,
															  LocalDate startDate,
															  LocalDate endDate,
															  Pageable pageable) {
		
		QRentalItem rentalItem = QRentalItem.rentalItem;
		QRental rental = QRental.rental;
		
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(rentalItem.sellerId.eq(sellerId));
		builder.and(rentalItem.productId.eq(productId));
		
		if (status != null) {
			
			builder.and(rentalItem.status.eq(status));
		}
		
		if (startDate != null) {
			
			builder.and(rentalItem.endDate.goe(startDate));
		}
		
		if (endDate != null) {
			
			builder.and(rentalItem.endDate.loe(endDate));
		}
		
		JPAQuery<RentalItem> query = queryFactory
				.selectFrom(rentalItem)
				.distinct()
				.join(rentalItem.rental, rental).fetchJoin()
				.where(builder)
				.orderBy(rentalItem.endDate.desc(), rentalItem.id.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize());
		
		List<RentalItem> rentalItems = query.fetch();
		
		Long total = queryFactory
				.select(rentalItem.id.countDistinct()).from(rentalItem).join(rentalItem.rental, rental)
				.where(builder)
				.fetchOne();
		
		long totalCount = (total != null) ? total : 0L;
		
		return new PageImpl<>(rentalItems, pageable, totalCount);
	}
	
	@Override
	public Page<RentalItem> findCompletedRentalItemsBySeller(Long sellerId,
															 LocalDate startDate,
															 LocalDate endDate,
															 Pageable pageable) {
		
		QRentalItem rentalItem = QRentalItem.rentalItem;
		QRental rental = QRental.rental;
		
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(rentalItem.sellerId.eq(sellerId));
		builder.and(rentalItem.status.eq(RentalItemStatus.RETURNED));
		
		if (startDate != null) {
			
			builder.and(rentalItem.endDate.goe(startDate));
		}
		
		if (endDate != null) {
			
			builder.and(rentalItem.endDate.loe(endDate));
		}
		
		JPAQuery<RentalItem> query = queryFactory
				.selectFrom(rentalItem)
				.distinct()
				.join(rentalItem.rental, rental)
				.where(builder)
				.orderBy(rentalItem.endDate.desc(), rentalItem.id.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize());
		
		List<RentalItem> rentalItems = query.fetch();
		
		Long total = queryFactory
				.select(rentalItem.id.countDistinct()).from(rentalItem).join(rentalItem.rental, rental)
				.where(builder)
				.fetchOne();
		
		long totalCount = (total != null) ? total : 0L;
		
		return new PageImpl<>(rentalItems, pageable, totalCount);
	}
	
	@Override
	public List<Long> findUnavailableProductIds(LocalDate startDate, LocalDate endDate, List<Long> productIds) {
	
		QRentalItem rentalItem = QRentalItem.rentalItem;
		
		return queryFactory
				.select(rentalItem.productId)
				.from(rentalItem)
				.where(
						rentalItem.productId.in(productIds),
						rentalItem.status.in(UNAVAILABLE_STATUSES),
						rentalItem.startDate.loe(endDate),
						rentalItem.endDate.goe(startDate)
				)
				.distinct()
				.fetch();
	}
	
	@Override
	public boolean existsOverlappingRentalItem(Long productId, LocalDate startDate, LocalDate endDate, Long excludeRentalItemId) {
		
		QRentalItem rentalItem = QRentalItem.rentalItem;
		
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(rentalItem.productId.eq(productId));
		builder.and(rentalItem.status.in(UNAVAILABLE_STATUSES));
		builder.and(rentalItem.startDate.loe(endDate));
		builder.and(rentalItem.endDate.goe(startDate));
		
		if (excludeRentalItemId != null) {
			
			builder.and(rentalItem.id.ne(excludeRentalItemId));
		}
		
		Integer exists = queryFactory
				.selectOne()
				.from(rentalItem)
				.where(builder)
				.fetchFirst();
		
		return exists != null;
	}
}
