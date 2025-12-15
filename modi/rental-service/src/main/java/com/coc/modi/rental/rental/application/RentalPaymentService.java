package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.exception.RentalAccessDeniedException;
import com.coc.modi.rental.rental.exception.RentalNotFoundException;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;
import com.coc.modi.rental.rental.infrastructure.client.AccountFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.WalletInfoResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalPaymentService {
	
	private final RentalRepository rentalRepository;
	private final AccountFeignClient accountFeignClient;
	private final RentalEventLogService rentalEventLogService;
	
	@Transactional
	public PayRentalResponse completePayment(Long rentalId, Long memberId) {
		
		Rental rental = rentalRepository.findById(rentalId)
				.orElseThrow(() -> new RentalNotFoundException(rentalId));
		
		if (!rental.getMemberId().equals(memberId)) {
			throw RentalAccessDeniedException.memberMismatch(rental.getId(), memberId);
		}
		
		if (rental.getItems() == null || rental.getItems().isEmpty()) {
			throw new RentalStatusInvalidException("결제할 대여 상품이 없습니다. rentalId=" + rentalId);
		}
		
		// 승인 상태/총액 정합성
		rental.recalculateAmountsAndStatus();
		
		RentalStatus rentalStatus = rental.getStatus();
		if (rentalStatus == RentalStatus.CANCELED || rentalStatus == RentalStatus.COMPLETED) {
			throw new RentalStatusInvalidException(
					"취소/완료된 대여는 결제 불가. rentalId=" + rentalId + ", status=" + rentalStatus);
		}
		
		if (rentalStatus != RentalStatus.ACCEPTED) {
			throw new RentalStatusInvalidException(
					"모든 아이템이 승인(ACCEPTED)일 때만 결제 가능. rentalId=" + rentalId + ", status=" + rentalStatus);
		}
		
		BigDecimal totalAmount = rental.getTotalAmount();
		
		WalletInfoResponse walletInfoResponse;
		try {
			walletInfoResponse = accountFeignClient.charge(new ChargeWalletCommand(memberId, rental.getId(), totalAmount));
		} catch (FeignException ex) {
			throw new RentalStatusInvalidException("결제 처리 중 지갑 서비스 호출에 실패했습니다.");
		}
		
		LocalDateTime paidAt = LocalDateTime.now();
		
		rental.getItems().stream()
				.filter(item -> item.getStatus() == RentalItemStatus.ACCEPTED)
				.forEach(RentalItem::markPaid);
		rental.markPaid(paidAt);
		
		rentalEventLogService.logEvent(rental, RentalEventType.PAID,
				Map.of("rentalId", rental.getId(),
						"paidAt", paidAt,
						"amount", totalAmount,
						"walletBalance", walletInfoResponse.balance(),
						"rentalStatus", rental.getStatus().name()));
		
		return PayRentalResponse.create(rental, totalAmount, walletInfoResponse.balance(), paidAt);
	}
}
