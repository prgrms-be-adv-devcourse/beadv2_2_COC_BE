# MODI 서비스 API 명세

본 문서는 member-service, account-service, seller-service, product-service, rental-service, support-service, ai-service의 컨트롤러 기반 API 명세입니다. 기본 응답은 `ApiResponse<T>`(`success:boolean, code:string, message:string, data:T`)이며, 일부 내부/관리자 API는 래퍼 없이 원본을 반환합니다. 리뷰 삭제는 204 No Content, 알림 스트림은 `text/event-stream`을 반환하며 채팅 메시지는 WebSocket(STOMP)로 전송됩니다. 인증/재발급/로그아웃 과정에서 리프레시 토큰은 HttpOnly 쿠키로 전달됩니다.

## 인증/권한
- JWT Bearer: 헤더 `Authorization: Bearer <accessToken>`
- 토큰 예외:
  - 회원가입 `POST /api/members/signup`
  - 인증 구간 `POST /api/auth/**` (단, `POST /api/auth/oauth2/connect`는 인증 필요)
  - OAuth2 구간 `/oauth2/**`, `/login/oauth2/**`
  - 결제/정적 페이지 `/toss-payment.html`, `/payments/**`
  - 판매자 조회 `GET /api/sellers/{sellerId}`
  - 상품 조회
    - `GET /product-service/api/products/search`
    - `GET /product-service/api/products/popular-keywords`
    - `GET /product-service/api/products/popular-products`
    - `GET /product-service/api/products/{productId}`
  - Swagger(`/swagger-ui/**`, `/v3/api-docs/**`)
  - Actuator(`/actuator/**`)
  - 내부용 `/internal/**`
- 기본 role 클레임: `MemberRole` 값(`MEMBER` 기본, `SELLER`)이 `role`로 담김. principal(CustomMember.memberId) 필요 API는 토큰 필수.
- 내부 API 인증: `/internal/**` 요청 시 내부 토큰 헤더 필요. 헤더명 `X-Internal-Token`(설정: `internal.api.header`), 값은 `internal.api.token`.

---
## member-service
### Enum
- AddressType: `MEMBER`, `SELLER`
- MemberRole: `MEMBER`, `SELLER`
- MemberStatus: `ACTIVE`, `INACTIVE`, `WITHDRAWN`

### 회원
- **POST /api/members/signup** — 회원가입
  - Req: `email:string(email)`, `password:string(8-20, 영문+숫자+특수문자)`, `name:string<=20`, `phone:string(휴대폰)`, `verificationToken:string`
  - Res: `MemberSignupResponse { email, name, phone, createdAt:datetime }`
- **GET /api/members/profile** — 내 정보 조회 (Auth)
  - Res: `MemberProfileResponse { id:long, name, phone }`
- **PUT /api/members/profile** — 내 정보 수정 (Auth)
  - Req: `name?:string<=20`, `phone?:string(휴대폰)`
  - Res: `MemberProfileResponse`
- **PATCH /api/members/passwords** — 비밀번호 수정 (Auth)
  - Req: `name:string`, `password:string(규칙 동일)`, `email:string`, `verificationCode:string(6)`
  - Res: `Void`
- **DELETE /api/members** — 회원 탈퇴 (Auth)
  - Res: `Void`

### 주소
- **GET /api/addresses/profile** — 내 배송지 목록 (Auth)
  - Res: `AddressResponse[] { addressId, addressLabel, recipientName, recipientPhone, type:AddressType, postcode, roadAddress, detailAddress, isDefault:boolean }`
- **POST /api/addresses/profile** — 배송지 등록 (Auth)
  - Req: `addressLabel:string<=30`, `recipientName:string<=20`, `recipientPhone:string(휴대폰)`, `type:AddressType`, `postcode:string(5)`, `roadAddress:string<=100`, `detailAddress:string<=100`, `isDefault:boolean`
  - Res: `Void` (201)
- **PUT /api/addresses/profile/{addressId}** — 배송지 수정 (Auth)
  - Path: `addressId:long`
  - Req: 각 필드 선택적(`addressLabel`, `recipientName`, `recipientPhone`, `type`, `postcode`, `roadAddress`, `detailAddress`, `isDefault:boolean`)
  - Res: `Void`
- **DELETE /api/addresses/profile/{addressId}** — 배송지 삭제 (Auth)
  - Path: `addressId:long`
  - Res: `Void`

### 인증
- **POST /api/auth/login**
  - Req: `email`, `password:string(8-20 영문/숫자/특수문자)`
  - Res: `String accessToken` (body), 리프레시 토큰 HttpOnly 쿠키 발급
- **POST /api/auth/reissue** — 토큰 재발급
  - Req: 리프레시 토큰 쿠키
  - Res: `String accessToken` (body), 리프레시 토큰 쿠키 재발급
- **POST /api/auth/logout**
  - Req: 리프레시 토큰 쿠키
  - Res: `Void` (리프레시 쿠키 제거)
- **POST /api/auth/email/verify/send**
  - Req: `email`
  - Res: `EmailVerificationSendResponse { result:"OK" }`
- **POST /api/auth/email/verify/confirm**
  - Req: `email`, `code:string(6)`
  - Res: `EmailVerificationConfirmResponse { verified:boolean, verificationToken:string }`
- **POST /api/auth/password/reset/send**
  - Req: `email`
  - Res: `Void`
- **POST /api/auth/password/reset/confirm**
  - Req: `email`, `code:string(6)`
  - Res: `PasswordResetConfirmResponse { resetToken }`
- **POST /api/auth/password/reset**
  - Req: `resetToken`, `newPassword:string(규칙 동일)`
  - Res: `Void`
- **POST /api/auth/oauth2/signup**
  - Req: `OAuth2SignupRequest`
  - Res: `String accessToken` (body), 리프레시 토큰 HttpOnly 쿠키 발급
- **POST /api/auth/oauth2/connect** — OAuth2 계정 연결 (Auth)
  - Req: `OAuth2ConnectRequest`
  - Res: `Void`

### 내부
- Auth: 내부 토큰 헤더 `X-Internal-Token: <token>` (설정: `internal.api.header`, `internal.api.token`)
- **PATCH /internal/members/{memberId}/role** — 판매자 롤로 변경 및 새 액세스 토큰 발급
  - Res: `String accessToken` (래퍼 없음)
- **PATCH /internal/members/{memberId}/status** — 회원 상태 변경
  - Req: `status:MemberStatus`
  - Res: `Void`
- **GET /internal/members/{memberId}/authz** — 역할 목록 조회
  - Res: `MemberAuthzResponse { memberId, roles:string[] }`
- **GET /internal/members/{memberId}/email** — 이메일 조회
  - Res: `MemberEmailResponse { memberId, email }`
- **GET /internal/members** — 회원 목록 조회
  - Query: `pageable`
  - Res: `MemberPageResponse { content:MemberSummaryResponse[], totalPages, totalElements, size, number, ... }`
- **GET /internal/members/{memberId}** — 회원 요약 조회
  - Res: `MemberSummaryResponse { memberId, email, name, phone, role, status, createdAt, updatedAt }`
- **GET /internal/members/search** — 이메일로 회원 조회
  - Query: `email:string`
  - Res: `MemberSummaryResponse`
- **POST /internal/members/batch** — 회원 ID 목록 조회
  - Req: `memberIds:long[]`
  - Res: `MemberSummaryResponse[]`
- **POST /internal/members/admin** — 관리자 계정 생성 (내부)
  - Req: `email:string(email)`, `password:string(8-20, 영문+숫자+특수문자)`, `name:string<=20`, `phone:string(휴대폰)`
  - Res: `InternalAdminMemberCreateResponse { memberId, email, role }`

---
## account-service
### Enum
- WalletTransactionType: `DEPOSIT_CHARGE`, `RENTAL_PAYMENT`, `RENTAL_REFUND`, `DEPOSIT_CANCEL`, `ADJUST`
- PgDepositStatus: `REQUESTED`, `SUCCESS`, `FAILED`, `CANCELED`

### 지갑
- **GET /api/accounts/balance** — 내 예치금 (Auth)
  - Res: `MemberWalletResponse { balance:decimal, createdAt:datetime }`
- **GET /api/accounts/transactions** — 내 거래내역 (Auth)
  - Res: `WalletTransactionResponse[] { txType:WalletTransactionType, amount, balanceAfter, relatedRentalId?:long, relatedSettlementId?:long, description, createdAt, paymentKey?:string, pgTid?:string }`
- **POST /api/accounts/withdrawals** — 출금 요청 (Auth)
  - Req: `amount:decimal>0`
  - Res: `WithdrawalResponse { id, status:WithdrawalStatus, requestedAmount, feeAmount, payoutAmount, requestedAt, processedAt }`

### PG 예치금
- **POST /api/deposits/pg/request** — 충전 요청 (Auth)
  - Req: `amount:decimal>0`
  - Res: `DepositResponse { id, memberId, amount, feeAmount, totalAmount, status:PgDepositStatus, pgProvider, orderId, requestedAt, approvedAt, failedReason, paymentKey }`
- **POST /api/deposits/pg/approve** — 결제 승인
  - Req: `paymentKey`, `orderId`, `amount:decimal`
  - Res: `DepositResponse`
- **GET /api/deposits/pg/config** — 위젯 설정
  - Res: `TossConfigResponse { clientKey, successUrl, failUrl }`
- **POST /api/deposits/pg/cancel** — 충전 취소/환불 (Auth)
  - Req: `paymentKey`, `orderId`, `amount:decimal`, `reason:string`
  - Res: `DepositResponse`
- **POST /api/deposits/pg/payments/fail** — 충전 실패 처리
  - Req: `orderId`, `code`, `message`
  - Res: `DepositResponse`

### 내부 지갑 (래퍼 없음)
- Auth: 내부 토큰 헤더 `X-Internal-Token: <token>` (설정: `internal.api.header`, `internal.api.token`)
- **GET /internal/wallets/{memberId}/balance** — 지갑 잔액 조회
  - Path: `memberId:long`
  - Res: `MemberWalletResponse { balance:decimal, createdAt:datetime }`
- **POST /internal/wallets/rental-payment** — 대여 결제 차감
  - Req: `memberId:long`, `rentalId:long`, `amount:decimal`
  - Res: `RentalPaymentResponse { walletId, memberId, balance:decimal }`
- **POST /internal/wallets/rental-refund** — 대여 환불 적립
  - Req: `memberId:long`, `rentalId:long`, `rentalItemId:long`, `amount:decimal`
  - Res: `RentalPaymentResponse`

---
## seller-service
### Enum
- SellerStatus: `ACTIVE`, `SUSPENDED`, `CLOSED`
- SellerSettlementStatus: `READY`, `PAID`, `CANCELED`
- SettlementBatchStatus: `READY`, `CALCULATING`, `COMPLETED`, `FAILED`

### 판매자 기본
- **POST /api/sellers** — 판매자 등록 (Auth)
  - Req: `storeName:string<=50`, `bizRegNo?:string<=20`, `storePhone?:string<=20`
  - Res: `SellerDetailResponse { sellerId, memberId, storeName, bizRegNo, storePhone, status:SellerStatus, createdAt, updatedAt }`
- **GET /api/sellers/self** — 내 판매자 (Auth)
  - Res: `SellerDetailResponse`
- **GET /api/sellers/{sellerId}** — 판매자 조회 (Auth)
  - Path: `sellerId:long`
  - Res: `SellerDetailResponse`
- **GET /api/sellers/self/rentals** — 내 대여 목록 (Auth)
  - Query: `productId?:long`, `status:string`, `startDate:yyyy-MM-dd`, `endDate:yyyy-MM-dd`, `page?:int`, `size?:int`
  - Res: `SellerRentalResponse[] { rentalItemId, productId, memberId, sellerId, status, totalAmount:decimal, startDate, endDate, paidAt }`
- **PUT /api/sellers/self** — 내 판매자 수정 (Auth)
  - Req: `storeName:string<=50`, `bizRegNo?:string<=20`, `storePhone?:string<=20`
  - Res: `SellerDetailResponse`
- **GET /api/sellers/products/{productId}** — 상품 요약 조회 (Auth)
  - Res: `ProductSummaryResponse { productId, productName, thumbnailImageUrl }`

### 내부 판매자 (래퍼 없음)
- Auth: 내부 토큰 헤더 `X-Internal-Token: <token>` (설정: `internal.api.header`, `internal.api.token`)
- **GET /internal/sellers/by-member/{memberId}** — 판매자 ID 조회
  - Res: `SellerIdResponse { sellerId, memberId }`
- **GET /internal/sellers/{sellerId}** — 판매자 조회
  - Res: `SellerDetailResponse`
- **PATCH /internal/sellers/{memberId}/approve** — 판매자 승인(내부)
  - Query: `approvedBy:long`
  - Res: `SellerRegistrationResponse { registrationId, memberId, storeName, bizRegNo, storePhone, status, approvedBy }`
- **PATCH /internal/sellers/{memberId}/reject** — 판매자 반려(내부)
  - Res: `SellerRegistrationResponse`
- **GET /internal/sellers/registrations** — 판매자 등록 요청 목록(내부)
  - Query: `status?:SellerRegistrationStatus`, `pageable`
  - Res: `SellerRegistrationPageResponse { content:SellerRegistrationResponse[], page, size, totalElements, totalPages, last }`

### 채팅
- **POST /api/chat/rooms** — 채팅방 생성 (Auth)
  - Req: `sellerId:long`, `memberId:long`
  - Res: `ChatRoomResponse { roomId, roomKey, sellerId, memberId, createdAt, updatedAt }`
- **GET /api/chat/rooms** — 채팅방 목록 조회 (Auth)
  - Res: `ChatRoomResponse[]`
- **GET /api/chat/rooms/{roomId}** — 채팅방 조회 (Auth)
  - Res: `ChatRoomResponse`
- **GET /api/chat/rooms/{roomId}/messages** — 채팅 메시지 조회 (Auth)
  - Query: `cursorId?:long`, `size?:int`
  - Res: `ChatMessageSliceResponse { messages:ChatMessageResponse[], nextCursorId, hasNext }`
  - `ChatMessageResponse { messageId, roomId, senderId, senderRole, content, sentAt }`
- **POST /api/chat/rooms/{roomId}/leave** — 채팅방 나가기 (Auth)
  - Res: `Void`

### 채팅(WebSocket/STOMP)
- **SEND /app/chat/rooms/{roomId}/send** — 메시지 전송
  - Payload: `ChatMessageSendRequest { content }`
  - Res: `Void`

### 판매자 정산(셀프)
- **GET /api/settlements/sellers/self** — 내 정산 목록 (Auth)
  - Query: `periodYm?:yyyy-MM`, `pageable`
  - Res: `Page<SellerSettlementResponse { id, batchId, sellerId, periodYm, totalRentalAmount, totalFeeAmount, settlementAmount, status:SellerSettlementStatus, paidAt, failureReason, createdAt, updatedAt }>`
- **GET /api/settlements/sellers/self/{sellerSettlementId}** — 단건 (Auth)
  - Res: `SellerSettlementResponse`
- **GET /api/settlements/sellers/self/{sellerSettlementId}/lines** — 라인 상세 (Auth)
  - Res: `SellerSettlementLineResponse[] { id, sellerSettlementId, sellerId, rentalItemId, memberId, productId, rentalAmount, feeAmount }`

### 정산 관리자 (Auth)
- **GET /api/admin/settlements/seller-settlements** — 판매자 정산 조회
  - Query: `periodYm?:yyyy-MM`, `sellerId?:long`, `status?:SellerSettlementStatus`, `pageable`
  - Res: `Page<SellerSettlementResponse>`
- **POST /api/admin/settlements/seller-settlements/{sellerSettlementId}/pay** — 관리자 지급 처리
  - Query: `paidAt?:ISO_LOCAL_DATE_TIME`(기본 now)
  - Res: `SellerSettlementResponse`
- **POST /api/admin/settlements/seller-settlements/pay-bulk** — 관리자 일괄 지급/실패 처리
  - Req: `SettlementBulkPayRequest { sellerId?:long, periodYm?:yyyy-MM, status?:SellerSettlementStatus, paidAt?:ISO_LOCAL_DATE_TIME }`
  - Res: `SettlementBulkPayResponse { requestedCount, successCount, failedCount }`

### 정산 배치 내부 (래퍼 ApiResponse)
- Auth: 내부 토큰 헤더 `X-Internal-Token: <token>` (설정: `internal.api.header`, `internal.api.token`)
- **GET /internal/settlements/batches** — 배치 목록
  - Query: `periodYm?:string`, `pageable`
  - Res: `Page<SettlementBatchResponse>`
- **GET /internal/settlements/batches/{batchId}** — 배치 단건
  - Res: `SettlementBatchResponse`

### 정산 관리자 (래퍼 ApiResponse)
- Auth: 내부 토큰 헤더 `X-Internal-Token: <token>` (설정: `internal.api.header`, `internal.api.token`)
- **GET /internal/settlements/seller-settlements** — 판매자 정산 전체 조회
  - Query: `periodYm?:yyyy-MM`, `pageable`
  - Res: `Page<SellerSettlementResponse>`
- **POST /internal/settlements/seller-settlements/{sellerSettlementId}/pay** — 관리자 지급 처리
  - Query: `paidAt?:ISO_LOCAL_DATE_TIME`(기본 now)
  - Res: `SellerSettlementResponse`

---
## product-service
### Enum
- ProductCategory: `LAPTOP`, `DESKTOP`, `CAMERA`, `TABLET`, `MOBILE`, `MONITOR`, `ACCESSORY`, `DRONE`, `AUDIO`, `PROJECTOR`
- ProductStatus: `ACTIVE`, `INACTIVE`, `DELETE`
- ProductSortType: `LATEST`, `OLDEST`, `PRICE_HIGH`, `PRICE_LOW`
- ProductModerationStatus: `PENDING`, `CLEAR`, `REVIEW`, `BLOCKED`

### 상품
- **GET /api/products/search** — 상품 스크롤 목록
  - Query: `keyword?`, `category?:ProductCategory`, `minPrice?:decimal`, `maxPrice?:decimal`, `sellerId?:long`, `startDate?:date`, `endDate?:date`, `cursor?:string`, `size:int=20`, `sortType:ProductSortType=LATEST`
  - Res: `ProductScrollResponse { products:ProductListResponse[], nextCursor:string, hasNext:boolean }`, `ProductListResponse { productId, name, pricePerDay, securityDepositAmount, status:ProductStatus, sellerId, thumbnailUrl }`
- **POST /api/products/bulk** — 상품 다건 조회
  - Req: `ProductBulkRequest { productIds:long[] }`
  - Res: `ProductListResponse[]`
- **GET /api/products/recent-searches** — 최근 검색어 (Auth)
  - Query: `size?:int=10`
  - Res: `string[]`
- **GET /api/products/popular-keywords** — 인기 검색어
  - Query: `size?:int=10`, `startDate?:date(yyyy-MM-dd)`, `endDate?:date(yyyy-MM-dd)`
  - Note: `startDate`와 `endDate`가 모두 없으면 오늘 하루 기준으로 집계
  - Res: `PopularKeywordResponse[] { keyword:string, count:long }`
- **GET /api/products/popular-products** — 인기 상품
  - Query: `size?:int=10`, `startDate?:date(yyyy-MM-dd)`, `endDate?:date(yyyy-MM-dd)`
  - Note: `startDate`와 `endDate`가 모두 없으면 오늘 하루 기준으로 집계
  - Res: `PopularProductResponse[] { productId:long, productName:string, viewCount:long }`
- **GET /api/products/seller** — 내 상품 목록 (Auth)
  - Query: `pageable`(size=20, sort=createdAt,desc 기본)
  - Res: `Page<ProductListResponse>`
- **GET /api/products/{productId}** — 상품 상세
  - Res: `ProductDetailResponse { productId, sellerId, name, description, pricePerDay, securityDepositAmount, status:ProductStatus, category, thumbnailImageId, specs:map<string,string>, images:ImageInfo[] }`
  - `ImageInfo { imageId, url, ordering }`
- **POST /api/products** — 상품 등록 (Auth)
  - Req: `name`, `description`, `pricePerDay:decimal>0`, `securityDepositAmount:decimal>=0`, `category:ProductCategory`, `specs?:map<string,string>`, `images?:string[]`
  - Res: `ProductDetailResponse` (201)
- **PUT /api/products/{productId}** — 상품 수정 (Auth)
  - Req: `name`, `description`, `pricePerDay:decimal>0`, `securityDepositAmount:decimal>=0`, `category`, `specs?:map<string,string>`, `images?:ImageInfo[] (imageId?, url, ordering)`
  - Res: `ProductDetailResponse`
- **PATCH /api/products/{productId}/active** — 활성화 (Auth)
  - Res: `Void`
- **PATCH /api/products/{productId}/inactive** — 숨김 (Auth)
  - Res: `Void`
- **DELETE /api/products/{productId}** — 삭제 (Auth)
  - Res: `Void`

### 이미지
- **POST /api/images/upload** — 이미지 업로드 (Auth)
  - Req: multipart `file`, query `dir?:string`
  - Res: `String`(imageUrl) (201)

### 관리자
- **GET /api/admin/products/moderation-requests** — 상품 검수 요청 목록 (Auth)
  - Query: `moderationStatus:ProductModerationStatus=PENDING`, `pageable`(size=20, sort=createdAt,asc 기본)
  - Res: `Page<ProductModerationSummaryResponse { productId, name, sellerId, status:ProductStatus, moderationStatus:ProductModerationStatus, createdAt }>`
- **POST /api/admin/products/{productId}/moderation-requests** — 상품 검수 요청 생성 (Auth)
  - Path: `productId:long`
  - Res: `Void`
- **PATCH /api/admin/products/{productId}/moderation/approve** — 상품 수동 승인 (Auth)
  - Path: `productId:long`
  - Query: `reason?:string`
  - Res: `Void`

### 내부 상품 (래퍼 없음)
- Auth: 내부 토큰 헤더 `X-Internal-Token: <token>` (설정: `internal.api.header`, `internal.api.token`)
- **POST /internal/products/bulk** — 상품 다건 조회
  - Req: `productIds:long[]` (body는 ID 배열)
  - Res: `ProductBulkResponse[] { productId, sellerId, price:decimal, securityDepositAmount:decimal, status:string }`
- **GET /internal/products/{productId}** — 상품 단건 조회
  - Path: `productId:long`
  - Res: `ProductInternalSellerResponse { productId, productName, thumbnailImageUrl }`
- **GET /internal/products/{productId}/embedding** — 임베딩 대상 조회
  - Path: `productId:long`
  - Res: `ProductEmbeddingResponse { productId, name, description, category, specs:map<string,string>, status }`
- **GET /internal/products/embedding-ids** — 임베딩 대상 ID 목록
  - Res: `long[]`
- **GET /internal/products/recent-viewed** — 회원 최근 조회 상품 ID (중복 제거)
  - Query: `memberId:long`, `limit:int=10`
  - Res: `long[]`

---
## rental-service
### Enum
- RentalStatus: `REQUESTED`, `PARTIALLY_ACCEPTED`, `ACCEPTED`, `PAID`, `IN_PROGRESS`, `CANCELED`, `COMPLETED`
- RentalItemStatus: `REQUESTED`, `ACCEPTED`, `REJECTED`, `PAID`, `RENTING`, `RETURNED`, `CANCELED`

### 대여 생성/결제/상태
- **POST /api/rentals/carts** — 장바구니 대여 생성 (Auth)
  - Req: `cartItemIds:long[] (>0)`
  - Res: `Void` (201)
- **POST /api/rentals** — 바로 대여 생성 (Auth)
  - Req: `productId:long`, `startDate:date(오늘 이후)`, `endDate:date(오늘 이후)`
  - Res: `Void` (201)
- **PATCH /api/rentals/{rentalItemId}/accept** — 판매자 수락 (Auth)
  - Path: `rentalItemId:long`
  - Res: `Void`
- **PATCH /api/rentals/{rentalItemId}/reject** — 판매자 거절 (Auth)
  - Res: `Void`
- **POST /api/rentals/{rentalId}/pay** — 결제 완료 처리(보증금 포함) (Auth)
  - Path: `rentalId:long`
  - Res: `PayRentalResponse { rentalId, paidAt:datetime, amount:decimal, balance:decimal, rentalStatus:string }`
- **POST /api/rentals/{rentalItemId}/rent** — 대여 시작 처리 (Auth)
  - Path: `rentalItemId:long`
  - Res: `Void` (결제 완료된 아이템을 대여 시작 상태(RENTING)로 전환)
- **PATCH /api/rentals/{rentalItemId}/cancel** — 대여 취소 (Auth)
  - Path: `rentalItemId:long`
  - Res: `Void`
- **POST /api/rentals/{rentalItemId}/return** — 반납 처리(보증금 환불, 수수료 차감) (Auth)
  - Path: `rentalItemId:long`
  - Req: `damageFee?:decimal>=0`, `damageReason?:string<=255`, `lateFee?:decimal>=0`, `lateReason?:string<=255`, `memo?:string<=500`
  - Res: `RentalReturnResponse { rentalId, rentalItemId, status, extraFeeAmount:string }`
- **POST /api/rentals/{rentalItemId}/refund** — 환불 (Auth)
  - Path: `rentalItemId:long`
  - Res: `Void`
- **POST /api/rentals/{rentalItemId}/extend** — 대여 연장 (Auth)
  - Path: `rentalItemId:long`
  - Req: `newEndDate:date(오늘 이후)`
  - Res: `Void`
- **GET /api/rentals/{rentalId}** — 대여 상세 (Auth)
  - Path: `rentalId:long`
  - Res: `RentalResponse { rentalId, paidAt, items:RentalItemResponse[] }`, `RentalItemResponse { rentalItemId, productId, startDate, endDate, status, unitPrice:decimal, securityDepositAmount:decimal }`
- **GET /api/rentals** — 대여 검색 (Auth)
  - Query: `startDate?:date`, `endDate?:date`, `rentalStatus?:RentalStatus`
  - Res: `RentalResponse[]`
- **GET /api/rentals/{productId}/unavailable-dates** — 상품별 예약 불가 날짜
  - Path: `productId:long`
  - Query: `ym:yyyy-MM`
  - Res: `UnavailableDatesResponse { productId, ym:yyyy-MM, unavailableDates:date[] }`

### 장바구니
- **GET /api/carts** — 내 장바구니 (Auth)
  - Res: `CartResponse { items:CartItemResponse[], updatedAt }`, `CartItemResponse { cartItemId, productId, startDate, endDate, price:decimal, status }`
- **POST /api/carts/items** — 아이템 추가 (Auth)
  - Req: `productId:long`, `startDate:date(오늘 이후)`, `endDate:date(오늘 이후)`
  - Res: `Void`
- **PUT /api/carts/me/items/{cartItemId}** — 아이템 수정 (Auth)
  - Path: `cartItemId:long`
  - Req: `startDate`, `endDate`
  - Res: `Void`
- **DELETE /api/carts/me/items/{cartItemId}** — 아이템 삭제 (Auth)
  - Path: `cartItemId:long`
  - Res: `Void`

### 내부 대여 (래퍼 없음)
- Auth: 내부 토큰 헤더 `X-Internal-Token: <token>` (설정: `internal.api.header`, `internal.api.token`)
- **GET /internal/rentals** — 판매자/기간별 대여 아이템 목록
  - Query(ModelAttribute): `sellerId:long`, `productId?:long`, `status:RentalItemStatus`, `startDate:date`, `endDate:date`, `pageable`
  - Res: `RentalItemInfoListResponse { rentalItemInfoList:RentalItemInfo[], totalCount:long, totalPages:int }`
  - `RentalItemInfo { rentalItemId, productId, memberId, sellerId, status, totalAmount:decimal, startDate, endDate, paidAt }`
- **GET /internal/rentals/items/{rentalItemId}/info** — 대여 아이템 상세 조회
  - Res: `RentalItemInfo { rentalItemId, productId, memberId, sellerId, status, totalAmount:decimal, startDate, endDate, paidAt }`
- **POST /internal/rentals/unavailable-products** — 기간 중 대여 불가 상품
  - Req: `startDate:date(오늘 이후)`, `endDate:date(오늘 이후)`, `productIds:long[]`
  - Res: `UnavailableProductsResponse { unavailableProductIds:long[] }`
- **GET /internal/rentals/items/{rentalItemId}** — 대여 아이템 판매자 정보
  - Res: `RentalItemSellerResponse`

---
## support-service
### Enum
- DeliveryStatus: `REGISTERED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `EXCEPTION`, `CANCELLED`
- NoticeStatus: `DRAFT`, `PUBLISHED`, `DELETED`
- BlacklistStatus: `ACTIVE`, `SUSPENDED`

### 리뷰
- **POST /api/reviews** — 판매자 리뷰 작성 (Auth)
  - Req: `rentalItemId:long`, `sellerId:long`, `rating:short(1~5)`, `content:string`
  - Res: `ReviewResponse { reviewId, rentalItemId, sellerId, memberId, rating, content, createdAt, updatedAt }` (201)
- **PATCH /api/reviews/{reviewId}** — 리뷰 수정 (Auth)
  - Req: `rating?:short(1~5)`, `content?:string`
  - Res: `ReviewResponse`
- **DELETE /api/reviews/{reviewId}** — 리뷰 삭제(소프트) (Auth)
  - Res: `204 No Content` (래퍼 없음)
- **GET /api/reviews** — 판매자 리뷰 목록
  - Query: `sellerId:long`, `rating?:int`, `pageable`
  - Res: `ReviewListResponse[] { reviewId, rentalItemId, sellerId, memberId, rating, content, createdAt }`
- **GET /api/reviews/me** — 내가 작성한 리뷰 목록 (Auth)
  - Query: `rating?:int`, `pageable`
  - Res: `ReviewListResponse[]`
- **GET /api/reviews/summary** — 리뷰 요약 조회
  - Query: `sellerId:long`
  - Res: `ReviewSummaryResponse { sellerId, summary, reviewCount, summarizedAt }` or `204 No Content`

### 알림
- **GET /api/notifications/stream** — 알림 SSE 구독 (Auth)
  - Res: `SseEmitter` stream(`text/event-stream`), 클라이언트는 Last-Event-ID 지원 시 재연결 처리

### 배송
- **POST /api/deliveries** — 배송 등록 (Auth)
  - Req: `rentalItemId:long`, `carrierCode:string<=30`, `trackingNumber:string<=50`
  - Res: `DeliveryCreateResponse { deliveryId, rentalItemId, carrierCode, trackingNumber, status:DeliveryStatus }` (201)
- **PATCH /api/deliveries/{rentalItemId}** — 배송 수정 (Auth)
  - Path: `rentalItemId:long`
  - Req: `carrierCode:string<=30`, `trackingNumber:string<=50`
  - Res: `DeliveryDetailResponse { deliveryId, rentalItemId, carrierCode, trackingNumber, status:DeliveryStatus, statusRaw, createdAt:datetime, updatedAt:datetime }`
- **GET /api/deliveries/{deliveryId}** — 배송 단건 조회 (Auth)
  - Path: `deliveryId:long`
  - Res: `DeliveryDetailResponse { deliveryId, rentalItemId, carrierCode, trackingNumber, status:DeliveryStatus, statusRaw, createdAt:datetime, updatedAt:datetime }`
- **GET /api/deliveries/rental-items/{rentalItemId}** — 대여 아이템 배송 단건 조회 (Auth)
  - Path: `rentalItemId:long`
  - Res: `DeliveryDetailResponse`

### 공지
- **GET /api/notices** — 공지 목록
  - Query: `keyword?:string`, `pageable`(sort=pinned,createdAt desc)
  - Res: `Page<NoticeSummaryResponse { id, title, pinned, viewCount, createdAt }>`
- **GET /api/notices/{noticeId}** — 공지 상세
  - Res: `NoticeResponse { id, title, content, status:NoticeStatus, pinned, viewCount, displayStartAt, displayEndAt, createdAt, updatedAt }`

### 관리자 - 공지 (Auth, ADMIN)
- **GET /api/admin/notices** — 공지 목록 조회(관리자)
  - Query: `status?:NoticeStatus`, `keyword?:string`, `pageable`(sort=pinned,createdAt desc)
  - Res: `Page<AdminNoticeSummaryResponse { id, title, pinned, viewCount, status, createdAt }>`
- **GET /api/admin/notices/{noticeId}** — 공지 상세 조회(관리자)
  - Res: `NoticeResponse { id, title, content, status:NoticeStatus, pinned, viewCount, displayStartAt, displayEndAt, createdAt, updatedAt }`
- **POST /api/admin/notices** — 공지 생성
  - Req: `title:string<=200`, `content:string`, `pinned?:boolean`, `status?:NoticeStatus`, `displayStartAt?:datetime`, `displayEndAt?:datetime`
  - Res: `NoticeResponse` (201)
- **PATCH /api/admin/notices/{noticeId}** — 공지 수정
  - Req: `title?:string<=200`, `content?:string`, `pinned?:boolean`, `displayStartAt?:datetime`, `displayEndAt?:datetime`
  - Res: `NoticeResponse`
- **DELETE /api/admin/notices/{noticeId}** — 공지 삭제
  - Res: `204 No Content` (래퍼 없음)
- **PATCH /api/admin/notices/{noticeId}/publish** — 공지 발행
  - Res: `NoticeResponse`
- **PATCH /api/admin/notices/{noticeId}/draft** — 공지 임시저장
  - Res: `NoticeResponse`

### 관리자 - 블랙리스트 (Auth, ADMIN)
- **GET /api/admin/blacklists** — 블랙리스트 목록
  - Query: `status?:BlacklistStatus`, `pageable`
  - Res: `Page<BlacklistSummaryResponse { memberId, email, name, status, suspendedAt, suspendedUntil, releasedAt }>`
- **GET /api/admin/blacklists/search** — 이메일 조회
  - Query: `email:string`
  - Res: `BlacklistSummaryResponse`
- **GET /api/admin/blacklists/{memberId}** — 블랙리스트 상세
  - Res: `BlacklistDetailResponse { memberId, email, name, phone, status, reason, memo, suspendedAt, suspendedUntil, releasedAt, createdAt, updatedAt }`
- **POST /api/admin/blacklists** — 블랙리스트 등록
  - Req: `BlacklistSuspendRequest { memberId:long, reason:string<=500, memo?:string<=1000 }`
  - Res: `BlacklistDetailResponse` (201)
- **PATCH /api/admin/blacklists/{memberId}/release** — 블랙리스트 해제
  - Req: `BlacklistReleaseRequest { memo?:string<=1000 }` (body optional)
  - Res: `BlacklistDetailResponse`

### 관리자 - 관리자 계정 (Auth, ADMIN)
- **POST /api/admin/members** — 관리자 계정 생성
  - Req: `email:string(email)`, `password:string(8-20, 영문+숫자+특수문자)`, `name:string<=20`, `phone:string(휴대폰)`
  - Res: `AdminMemberCreateResponse { memberId, email, role }`

### 관리자 - 판매자 승인 (Auth, ADMIN)
- **PATCH /api/admin/sellers/{memberId}/approve** — 판매자 승인
  - Path: `memberId:long`
  - Res: `ApiResponse<SellerRegistrationResponse { registrationId, memberId, storeName, bizRegNo, storePhone, status, approvedBy }>`
- **PATCH /api/admin/sellers/{memberId}/reject** — 판매자 반려
  - Path: `memberId:long`
  - Res: `ApiResponse<SellerRegistrationResponse>`
- **GET /api/admin/sellers/registrations** — 판매자 등록 요청 목록
  - Query: `status?:SellerRegistrationStatus`, `pageable`
  - Res: `ApiResponse<Page<SellerRegistrationResponse>>`

---
## ai-service
### API

- **POST /api/ai/recommendations** — 추천 상품 조회
  - Req: `productId?:long`, `query?:string`, `categories?:string[]`, `size?:int>=1`
  - Res: `ProductRecommendationResponse { message, items:Item[] }`
  - `Item { productId, name, category, specs:map<string,string>, status, distance }`
- **GET /api/ai/recommendations/recent** — 최근 조회 기반 추천 상품 조회 (Auth)
  - Query: `size?:int>=1`
  - Res: `Item[]`
- **POST /api/ai/ai/chat-test** — AI 채팅 테스트
  - Req: `message:string`
  - Res: `String`
- **POST /api/ai/descriptions** — 상품 설명 추천
  - Req: `ProductDescriptionRequest`
  - Res: `String`
- **POST /api/ai/embeddings/reindex** — 임베딩 미생성 건 재색인
  - Res: `Integer`(재색인 개수)
- **POST /api/ai/{productId}/embedding** — 단건 임베딩 미생성 재색인
  - Res: `Boolean`
