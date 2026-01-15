🏷️ Rental & RentalItem 상태 (rental-service 기준)

`rental-service/src/main/java/com/coc/modi/rental/rental/domain`에 정의된 Enum과 서비스 로직을 기준으로 실제 동작을 정리했습니다. RentalStatus는 RentalItemStatus와 `paidAt`을 조합해 `Rental.calculateStatus()`에서 자동 계산됩니다.

## 1. RentalItemStatus (단일 아이템)
- REQUESTED: 생성 시 기본값. 판매자 승인/거절 가능, 회원 취소 가능.
- ACCEPTED: 판매자 승인(REQUESTED → ACCEPTED).
- REJECTED: 판매자 거절(REQUESTED → REJECTED). `canceledAt` 설정.
- CANCELED: 회원 취소(REQUESTED 또는 ACCEPTED → CANCELED). `canceledAt` 설정.
- PAID: 모든 아이템이 ACCEPTED 상태일 때 결제 성공 시 PaymentService가 설정.
- RENTING: 판매자가 인도 처리. 코드상 PAID 상태이면서 `LocalDate.now()`가 `startDate` 이후이면 예외를 던짐.
- RETURNED: RENTING → RETURNED. `returnedAt` 설정. 반납 후 환불 시 상태는 그대로 두고 `canceledAt`만 추가로 설정.
- 기타 제약: 연장은 PAID 또는 RENTING에서만 가능. 환불은 서비스에서 PAID/RETURNED 모두 시도 가능하지만 구현상 RETURNED가 아니면 예외가 발생한다.

### 상태 전환 흐름 (코드 기준)
```
REQUESTED --(seller accept)--> ACCEPTED --(payment)--> PAID --(startRenting)--> RENTING --(processReturn)--> RETURNED
    |  \                        \
    |   \--(seller reject)--> REJECTED
    \--(member cancel)--> CANCELED
```
*환불은 RETURNED 상태에서 `canceledAt`만 찍는 형태로 처리되며 상태 이름은 그대로 RETURNED.*

## 2. RentalStatus (대여 전체)
- REQUESTED: 기본값.
- PARTIALLY_ACCEPTED: chargeable 아이템 중 일부만 ACCEPTED인 경우.
- ACCEPTED: chargeable 아이템이 모두 ACCEPTED.
- PAID: `paidAt`이 존재하고, 진행/완료 조건에 걸리지 않는 경우.
- IN_PROGRESS: chargeable 아이템 중 하나라도 RENTING.
- COMPLETED: chargeable 아이템이 모두 RETURNED.
- CANCELED: 모든 아이템이 CANCELED/REJECTED이거나 chargeable 아이템이 하나도 없을 때(반납 후 환불로 `canceledAt`이 찍힌 경우 포함).

### Rental.calculateStatus() 요약
1) 아이템 없으면 REQUESTED.  
2) 모든 아이템이 CANCELED/REJECTED면 CANCELED.  
3) `chargeableItems = canceledAt이 없고, 상태가 CANCELED/REJECTED가 아닌 아이템`; 비어 있으면 CANCELED.  
4) `returnedCount == totalChargeable` 이면 COMPLETED(완료된 상태 우선 처리).  
5) RENTING이 하나라도 있으면 IN_PROGRESS.  
6) `paidAt != null`이면 PAID.  
7) chargeable이 모두 ACCEPTED면 ACCEPTED, 일부만 ACCEPTED면 PARTIALLY_ACCEPTED, 모두 REQUESTED면 REQUESTED, 그 외는 PARTIALLY_ACCEPTED.

## 3. 대표 시나리오
- 생성: 모든 아이템 REQUESTED → Rental REQUESTED.
- 일부만 승인: ACCEPTED와 REQUESTED 혼재 → Rental PARTIALLY_ACCEPTED.
- 전부 승인: 모든 chargeable 아이템 ACCEPTED → Rental ACCEPTED.
- 결제: 아이템 PAID, `paidAt` 설정 → Rental PAID.
- 인도(대여 시작): 아이템 RENTING → Rental IN_PROGRESS.
- 반납: 아이템 RETURNED → 모든 chargeable RETURNED면 Rental COMPLETED.
- 반납 후 환불: 상태는 RETURNED 유지, `canceledAt`만 세팅 → chargeable이 없으면 Rental CANCELED.

## 4. 책임 분리
- 개별 아이템 상태 전환은 도메인 메서드(RentalItem.decide, markPaid, startRenting 등)로만 수행.
- RentalStatus는 `updateStatusFromItems()`/`recalculateAmountsAndStatus()`에서만 갱신해야 하며 서비스 레이어가 직접 값을 넣지 않는다.
