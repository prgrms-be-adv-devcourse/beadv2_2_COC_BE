package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.dto.RentalItemResponse;
import com.coc.modi.rental.rental.application.dto.RentalResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalQueryService {

    private final RentalRepository rentalRepository;
    private final RentalQueryRepository rentalQueryRepository;

    public ResponseEntity<ApiResponse<RentalResponse>> getRentalDetails(Long rentalId) {

        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new IllegalArgumentException("해당 렌탈 Id와 일치하는 정보가 없습니다."));
        List<RentalItem> rentalItemList = rental.getItems();

        List<RentalItemResponse> rentalItemResponseList = rentalItemList.stream().map(RentalItemResponse :: from).toList();

        RentalResponse rentalResponse = RentalResponse.create(rental, rentalItemResponseList);

        return ResponseEntity.ok(ApiResponse.ok(rentalResponse));
    }

    public ResponseEntity<ApiResponse<List<RentalResponse>>> searchRentals(LocalDate startDate,
                                                                           LocalDate endDate,
                                                                           RentalStatus rentalStatus) {

        List<Rental> rentals = rentalQueryRepository.search(startDate, endDate, rentalStatus);

        List<RentalResponse> responses = rentals.stream()
                .map(rental -> {
                    List<RentalItemResponse> itemResponses = rental.getItems()
                            .stream()
                            .map(RentalItemResponse::from)
                            .toList();
                    return RentalResponse.create(rental, itemResponses);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
