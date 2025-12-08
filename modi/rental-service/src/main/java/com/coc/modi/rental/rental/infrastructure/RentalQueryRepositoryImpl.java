package com.coc.modi.rental.rental.infrastructure;

import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.domain.QRental;
import com.coc.modi.rental.rental.domain.QRentalItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RentalQueryRepositoryImpl implements RentalQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Rental> search(LocalDate startDate, LocalDate endDate, RentalStatus rentalStatus) {

        QRental rental = QRental.rental;
        QRentalItem rentalItem = QRentalItem.rentalItem;

        BooleanBuilder builder = new BooleanBuilder();

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
}
