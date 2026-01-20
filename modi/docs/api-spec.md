# API 명세서 (요약)

## 공통
- 게이트웨이 경로: `/{service-id}/api/**` 또는 `/{service-id}/internal/**`
- 인증: 외부 API는 `Authorization: Bearer <accessToken>` 사용
- 게이트웨이는 하위 서비스에 `X-Member-Id`, `X-Roles` 헤더를 주입
- 내부 API는 `X-Internal-Token` 헤더 필요 (`/internal/**`)
- 응답 포맷: 대부분 `ApiResponse` 사용 (SSE는 `text/event-stream`)

## member-service
서비스 ID: `member-service`

### 외부 API
| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/members/signup` | 회원가입 |
| GET | `/api/members/profile` | 내 프로필 조회 |
| PUT | `/api/members/profile` | 내 프로필 수정 |
| PATCH | `/api/members/passwords` | 비밀번호 변경 |
| DELETE | `/api/members` | 회원 탈퇴 |
| GET | `/api/addresses/profile` | 내 주소 목록 조회 |
| POST | `/api/addresses/profile` | 주소 등록 |
| PUT | `/api/addresses/profile/{addressId}` | 주소 수정 |
| DELETE | `/api/addresses/profile/{addressId}` | 주소 삭제 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/reissue` | 토큰 재발급 |
| POST | `/api/auth/logout` | 로그아웃 |
| POST | `/api/auth/email/verify/send` | 이메일 인증 코드 발송 |
| POST | `/api/auth/email/verify/confirm` | 이메일 인증 확인 |
| POST | `/api/auth/password/reset/send` | 비밀번호 재설정 코드 발송 |
| POST | `/api/auth/password/reset/confirm` | 비밀번호 재설정 코드 확인 |
| POST | `/api/auth/password/reset` | 비밀번호 재설정 |

### 내부 API
| Method | Path | 설명 |
| --- | --- | --- |
| PATCH | `/internal/members/{memberId}/role` | 회원 역할 변경 (SELLER) |

## account-service
서비스 ID: `account-service`

### 외부 API
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/accounts/balance` | 지갑 잔액 조회 |
| GET | `/api/accounts/transactions` | 지갑 거래 내역 조회 |
| POST | `/api/deposits/pg/request` | 예치금 충전 요청 |
| POST | `/api/deposits/pg/approve` | 예치금 충전 승인 |
| GET | `/api/deposits/pg/config` | 결제 위젯 설정 조회 |
| POST | `/api/deposits/pg/cancel` | 예치금 충전 취소/환불 |
| POST | `/api/deposits/pg/payments/fail` | 예치금 충전 실패 처리 |

### 내부 API
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/internal/wallets/{memberId}/balance` | 특정 회원 지갑 잔액 조회 |
| POST | `/internal/wallets/rental-payment` | 대여 결제 처리 |
| POST | `/internal/wallets/rental-refund` | 대여 환불 처리 |

## product-service
서비스 ID: `product-service`

### 외부 API
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/products` | 상품 검색/목록 |
| GET | `/api/products/seller` | 판매자 상품 목록 |
| GET | `/api/products/{productId}` | 상품 상세 조회 |
| POST | `/api/products` | 상품 등록 |
| PUT | `/api/products/{productId}` | 상품 수정 |
| PATCH | `/api/products/{productId}/active` | 상품 활성화 |
| PATCH | `/api/products/{productId}/inactive` | 상품 비활성화 |
| DELETE | `/api/products/{productId}` | 상품 삭제 |
| POST | `/api/images/upload` | 상품 이미지 업로드 |
| POST | `/api/products/recommendations` | 추천 상품 조회 |
| POST | `/api/products/ai/chat-test` | AI 추천 채팅 테스트 |
| POST | `/api/products/reindex` | 검색 인덱스 재생성 |
| GET | `/api/products/popular-keywords` | 인기 검색어 통계 |
| GET | `/api/products/popular-products` | 인기 상품 통계 |
| POST | `/api/products/embeddings/reindex` | 전체 임베딩 재생성 |
| POST | `/api/products/{productId}/embedding` | 단건 임베딩 재생성 |

### 내부 API
| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/internal/products/bulk` | 상품 일괄 조회 |
| GET | `/internal/products/{productId}` | 상품 단건 조회 |

## rental-service
서비스 ID: `rental-service`

### 외부 API
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/carts` | 내 장바구니 조회 |
| POST | `/api/carts/items` | 장바구니 아이템 추가 |
| PUT | `/api/carts/me/items/{cartItemId}` | 장바구니 아이템 수정 |
| DELETE | `/api/carts/me/items/{cartItemId}` | 장바구니 아이템 삭제 |
| POST | `/api/rentals` | 대여 생성 |
| POST | `/api/rentals/carts` | 장바구니 기반 대여 생성 |
| PATCH | `/api/rentals/{rentalItemId}/accept` | 대여 아이템 수락 |
| PATCH | `/api/rentals/{rentalItemId}/reject` | 대여 아이템 거절 |
| POST | `/api/rentals/{rentalId}/pay` | 대여 결제 완료 |
| PATCH | `/api/rentals/{rentalItemId}/cancel` | 대여 취소 |
| POST | `/api/rentals/{rentalItemId}/return` | 반납 처리 |
| POST | `/api/rentals/{rentalItemId}/refund` | 환불 처리 |
| POST | `/api/rentals/{rentalItemId}/extend` | 대여 기간 연장 |
| POST | `/api/rentals/{rentalItemId}/rent` | 대여 시작 |
| GET | `/api/rentals/{rentalId}` | 대여 상세 조회 |
| GET | `/api/rentals` | 대여 목록 조회 |
| GET | `/api/rentals/{productId}/unavailable-dates` | 대여 불가 날짜 조회 |

### 내부 API
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/internal/rentals` | 대여 아이템 목록 조회 |
| POST | `/internal/rentals/unavailable-products` | 대여 불가 상품 조회 |

## review-service
서비스 ID: `review-service`

### 외부 API
| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/reviews` | 리뷰 작성 |
| PATCH | `/api/reviews/{reviewId}` | 리뷰 수정 |
| DELETE | `/api/reviews/{reviewId}` | 리뷰 삭제(소프트 삭제) |
| GET | `/api/reviews/{reviewId}` | 리뷰 상세 조회 |
| GET | `/api/reviews?sellerId={sellerId}` | 판매자 리뷰 목록 조회 |
| GET | `/api/reviews/me` | 내 리뷰 목록 조회 |

## notification-service
서비스 ID: `notification-service`

### 외부 API
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/notifications/stream` | 알림 SSE 스트림 구독 |

## delivery-service
서비스 ID: `delivery-service`

### 외부 API
| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/deliveries` | 배송 등록 |
| GET | `/api/deliveries/{deliveryId}` | 배송 상세 조회 |

## seller-service
서비스 ID: `seller-service`

### 외부 API - 판매자
| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/sellers` | 판매자 등록 |
| GET | `/api/sellers/self` | 내 판매자 정보 조회 |
| PUT | `/api/sellers/self` | 내 판매자 정보 수정 |
| GET | `/api/sellers/self/rentals` | 판매자 대여 목록 조회 |
| GET | `/api/sellers/products/{productId}` | 상품 요약 조회 |

### 외부 API - 채팅
| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/chat/rooms` | 채팅방 생성 |
| GET | `/api/chat/rooms/{roomId}` | 채팅방 조회 |
| GET | `/api/chat/rooms/{roomId}/messages` | 채팅 메시지 조회 |

### 외부 API - 정산
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/settlements/sellers/self` | 정산 목록 조회 |
| GET | `/api/settlements/sellers/self/{sellerSettlementId}` | 정산 상세 조회 |
| GET | `/api/settlements/sellers/self/{sellerSettlementId}/lines` | 정산 상세 라인 조회 |
| POST | `/api/settlements/sellers/self/{sellerSettlementId}/pay` | 정산 지급 처리 |
| POST | `/api/settlements/sellers/self/{sellerSettlementId}/cancel` | 정산 취소 |
| POST | `/api/settlements/sellers/self/batches/run` | 정산 배치 실행 |

### 내부 API
| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/internal/sellers/by-member/{memberId}` | memberId로 sellerId 조회 |
| GET | `/internal/sellers/{sellerId}` | sellerId로 판매자 조회 |
| POST | `/internal/settlements/batches` | 정산 배치 생성 |
| POST | `/internal/settlements/batches/{batchId}/start` | 정산 배치 시작 |
| POST | `/internal/settlements/batches/{batchId}/complete` | 정산 배치 완료 |
| GET | `/internal/settlements/batches` | 정산 배치 목록 조회 |
| GET | `/internal/settlements/batches/{batchId}` | 정산 배치 상세 조회 |
