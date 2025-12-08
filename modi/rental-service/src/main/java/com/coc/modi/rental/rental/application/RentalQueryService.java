package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.application.dto.RentalItemResponse;
import com.coc.modi.rental.rental.application.dto.RentalResponse;
import com.coc.modi.rental.rental.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalQueryService {

    private final RentalRepository rentalRepository;
    private final RentalQueryRepository rentalQueryRepository;

    public RentalResponse getRentalDetails(Long rentalId) {

        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new IllegalArgumentException("해당 렌탈 Id와 일치하는 정보가 없습니다."));
        List<RentalItem> rentalItemList = rental.getItems();

        List<RentalItemResponse> rentalItemResponseList = rentalItemList.stream().map(RentalItemResponse :: from).toList();

        return RentalResponse.create(rental, rentalItemResponseList);
    }

    public List<RentalResponse> searchRentals(LocalDate startDate, LocalDate endDate, RentalStatus status) {

        List<Rental> rentals = rentalQueryRepository.search(startDate, endDate, status);

        return rentals.stream()
                .map(rental -> {
                    List<RentalItemResponse> itemResponses = rental.getItems()
                            .stream()
                            .map(RentalItemResponse::from)
                            .toList();
                    return RentalResponse.create(rental, itemResponses);
                })
                .toList();
    }
}
