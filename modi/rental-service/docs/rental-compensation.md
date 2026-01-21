# 렌탈 결제/환불/연장 보상 트랜잭션 정리

## 목적
- 렌탈 서비스의 로컬 트랜잭션이 롤백될 때, 이미 실행된 계정 서비스 결제/환불을 되돌리기 위한 보상 처리.
- 결제/환불 API 재시도 시 중복 처리를 막기 위한 `requestId` 부여.

## 적용 범위
- 결제 완료: `RentalPaymentService.completePayment`
- 환불: `RentalPaymentService.refundRentalItem`
- 연장 결제: `RentalLifecycleService.extendRentalItem`
- 요청 ID 생성: `WalletRequestId`
- 계정 서비스 호출 DTO: `ChargeWalletCommand`, `RefundWalletCommand` (requestId 포함)

## 공통 동작 흐름
1. 렌탈/아이템 상태 및 권한 검증.
2. 계정 서비스 호출(결제/환불) 시 `requestId` 포함.
3. `TransactionSynchronizationManager`에 롤백 보상 등록.
4. 도메인 상태 갱신 및 이벤트 로그 기록.
5. 트랜잭션이 롤백되면 `afterCompletion(STATUS_ROLLED_BACK)`에서 보상 호출 실행.

## requestId 규칙
| 구분 | requestId 포맷 | 사용처 |
| --- | --- | --- |
| 결제 | `rental-payment:{rentalId}` | 결제 시 charge |
| 환불 | `rental-refund:{rentalItemId}` | 환불 시 refund |
| 연장 결제 | `rental-extend:{rentalItemId}:{newEndDate}` | 연장 시 charge |
| 결제 롤백 보상 환불 | `rental-payment-comp-refund:{rentalId}:{rentalItemId}` | 결제 후 롤백 시 refund |
| 환불 롤백 보상 차지 | `rental-refund-comp-charge:{rentalItemId}` | 환불 후 롤백 시 charge |
| 연장 롤백 보상 환불 | `rental-extend-comp-refund:{rentalItemId}:{newEndDate}` | 연장 후 롤백 시 refund |

> 멱등성은 계정 서비스에서 `(tx_type, request_id)` 유니크 제약을 가진다는 전제에 의존한다.

## 시나리오별 상세

### 1) 결제 완료 (`completePayment`)
- 결제 대상: `ACCEPTED` 상태의 아이템.
- 결제 요청: `charge` 호출에 `requestId = rental-payment:{rentalId}` 사용.
- 롤백 보상:
  - 결제 전 `ACCEPTED` 아이템 목록과 금액을 캡처.
  - 롤백 시 아이템별로 `refund` 호출.
  - `requestId = rental-payment-comp-refund:{rentalId}:{rentalItemId}`.
- 보상 실패는 `log.error`로만 남기고 예외는 전파하지 않음(트랜잭션 이미 종료됨).

### 2) 환불 (`refundRentalItem`)
- 환불 요청: `refund` 호출에 `requestId = rental-refund:{rentalItemId}` 사용.
- 롤백 보상:
  - 롤백 시 동일 금액을 `charge`로 재차지.
  - `requestId = rental-refund-comp-charge:{rentalItemId}`.
- 보상 실패는 `log.error` 처리.

### 3) 연장 결제 (`extendRentalItem`)
- 연장 조건: `PAID` 또는 `RENTING` 상태.
- 연장 금액 산정 후 `charge` 호출.
  - `requestId = rental-extend:{rentalItemId}:{newEndDate}`.
- 롤백 보상:
  - 롤백 시 연장 금액을 `refund`.
  - `requestId = rental-extend-comp-refund:{rentalItemId}:{newEndDate}`.
- 보상 실패는 `log.error` 처리.

## 구현 포인트
- 보상 등록은 실제 트랜잭션이 활성화된 경우에만 수행.
- 롤백 보상은 `TransactionSynchronization.afterCompletion`에서 실행.
- 외부 호출이 실패해도 롤백 보상이 다시 실패할 수 있으므로, 보상은 best-effort로 처리.

## 관련 코드 위치
- `rental-service/src/main/java/com/coc/modi/rental/rental/application/RentalPaymentService.java`
- `rental-service/src/main/java/com/coc/modi/rental/rental/application/RentalLifecycleService.java`
- `rental-service/src/main/java/com/coc/modi/rental/rental/application/WalletRequestId.java`
- `rental-service/src/main/java/com/coc/modi/rental/rental/infrastructure/client/dto/ChargeWalletCommand.java`
- `rental-service/src/main/java/com/coc/modi/rental/rental/infrastructure/client/dto/RefundWalletCommand.java`
