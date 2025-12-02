﻿# 🇰🇷 Modi 렌탈·정산 백엔드 (beadv2_2_COC_BE)

> 사용자와 판매자를 연결해 전자기기 대여·반납·결제·정산을 처리하는 백엔드. settlement 도메인 담당.

## 📋 주요 특징
- 아키텍처: 도메인 주도 설계(DDD) + 클린 아키텍처 적용 예정 (`ddd-design` 자료, `shop-test` 스타일 참조)
- 공통 컴포넌트: ApiResponse, BaseEntity는 이론 자료 메모장 스타일 사용
- 데이터베이스: PostgreSQL
- 인프라/프레임워크: Spring Boot 기반 (추가 스택 확정 시 업데이트)
- 소스 관리: settlement 중심 모듈부터 설계/구현

## 🚀 빠른 시작
- 필수 스택: Java 17+, Gradle Wrapper, Spring Boot 3.4.x (확정 시 업데이트), DB: PostgreSQL 16
- 환경 변수 템플릿(`.env.example`):
  ```env
  SPRING_PROFILES_ACTIVE=local
  DB_HOST=localhost
  DB_PORT=5432
  DB_NAME=modi
  DB_USER=modi
  DB_PASSWORD=modi

  SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
  SPRING_DATASOURCE_USERNAME=${DB_USER}
  SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

  # Hibernate DDL: dev=update/none, prod=validate
  SPRING_JPA_HIBERNATE_DDL_AUTO=update
  ```
- PostgreSQL 로컬 실행(`docker-compose.yml` 예시):
  ```yaml
  version: "3.8"
  services:
    postgres:
      image: postgres:16
      container_name: modi-postgres
      environment:
        POSTGRES_DB: modi
        POSTGRES_USER: modi
        POSTGRES_PASSWORD: modi
      ports:
        - "5432:5432"
  ```
- 실행 예시: `docker compose up -d` → `./gradlew bootRun --args='--spring.profiles.active=local'`
- 테스트/빌드: `./gradlew test`, `./gradlew build`
- 프로필 가이드: `local`(개발, DDL update/none), `test`(통합테스트, create-drop/update), `prod`(운영, validate)
- DB 마이그레이션: Flyway/Liquibase 예정(`./gradlew flywayMigrate` 등으로 추가 예정)

## 🧭 프로젝트 소개
- 이름: **Modi 모디**
- 개요: 사용자와 판매자를 연결해 전자기기의 대여·반납·결제·정산을 통합 처리하는 렌탈 플랫폼
- 판매 상품: 노트북, 태블릿, 카메라 등 전자기기 대여; 판매자 개인 기기 등록 가능 (마켓플레이스)
- 주요 기능: [기능 정의서 Notion](https://www.notion.so/2bb9d0051b998053b0ffdadd465620f8?pvs=21)

## 📄 프로젝트 API 명세서 (정산 영역)
### 6-1. 정산 배치 조회 (관리자)
- **GET** `/api/settlements/batches?periodYm=2025-11`
  - 특정 월(periodYm) 정산 배치 리스트 조회 (검색 조건 없으면 전체)
  - Response 200 예시:
  ```json
  [
    {
      "batchId": 1,
      "periodYm": "2025-11",
      "status": "COMPLETED",
      "startedAt": "2025-12-01T00:00:01",
      "completedAt": "2025-12-01T00:02:30"
    }
  ]
  ```
- **GET** `/api/settlements/batches/{batchId}`
  - 단일 배치 상세 조회
  - Response 200 예시:
  ```json
  {
    "batchId": 1,
    "periodYm": "2025-11",
    "status": "COMPLETED",
    "startedAt": "2025-12-01T00:00:01",
    "completedAt": "2025-12-01T00:02:30"
  }
  ```

### 6-2. 판매자 정산 조회 (Seller)
- **GET** `/api/settlements/sellers/me?periodYm=2025-11`
  - 로그인 셀러의 정산 헤더 목록 조회 (periodYm 필터 가능)
  - Response 200 예시:
  ```json
  [
    {
      "sellerSettlementId": 1000,
      "periodYm": "2025-11",
      "totalRentalAmount": 1000000,
      "totalFeeAmount": 100000,
      "settlementAmount": 900000,
      "status": "READY",
      "paidAt": null
    }
  ]
  ```
- **GET** `/api/settlements/sellers/me/{sellerSettlementId}/lines`
  - 정산서 하나의 상세 라인 조회 (렌탈/상품 단위로 금액 확인)
  - Response 200 예시:
  ```json
  [
    {
      "lineId": 1,
      "rentalId": 500,
      "productId": 200,
      "rentalAmount": 45000,
      "feeAmount": 4500
    },
    {
      "lineId": 2,
      "rentalId": 501,
      "productId": 201,
      "rentalAmount": 30000,
      "feeAmount": 3000
    }
  ]
  ```

## 🗄️ 프로젝트 ERD (settlement-service)
### 6-1. SETTLEMENT_BATCH
| 컬럼명 | 타입 | 설명 | 참조관계 |
| --- | --- | --- | --- |
| id | BIGINT PK | 배치 ID | - |
| period_ym | CHAR(7) | 'YYYY-MM' 정산 기간 | - |
| status | VARCHAR(20) | CALCULATING, COMPLETED 등 | - |
| started_at | DATETIME | 배치 시작 일시 | - |
| completed_at | DATETIME NULL | 배치 완료 일시 | - |

### 6-2. SELLER_SETTLEMENT
| 컬럼명 | 타입 | 설명 | 참조관계 |
| --- | --- | --- | --- |
| id | BIGINT PK | 판매자 정산 ID | - |
| batch_id | BIGINT | 배치 ID | FK → SETTLEMENT_BATCH.batch_id |
| seller_id | BIGINT | 판매자 ID | 논리 FK → seller-service.SELLER.seller_id |
| total_rental_amount | DECIMAL(18,2) | 정산 대상 총 렌탈 금액 | - |
| total_fee_amount | DECIMAL(18,2) | 수수료 총액 | - |
| settlement_amount | DECIMAL(18,2) | 실제 정산 금액 | - |
| status | VARCHAR(20) | READY, PAID, CANCELED 등 | - |
| paid_at | DATETIME NULL | 정산금 지급 일시 | - |
| created_at | DATETIME | 생성 일시 | - |
| updated_at | DATETIME | 수정 일시 | - |

### 6-3. SELLER_SETTLEMENT_LINE
| 컬럼명 | 타입 | 설명 | 참조관계 |
| --- | --- | --- | --- |
| id | BIGINT PK | 정산 상세 ID | - |
| seller_settlement_id | BIGINT | 판매자 정산 ID | FK → SELLER_SETTLEMENT.seller_settlement_id |
| seller_id | BIGINT | 판매자 ID | 논리 FK → seller-service.SELLER.seller_id |
| rental_id | BIGINT | 렌탈 주문 ID | 논리 FK → rental-service.RENTAL_ORDER.rental_id |
| member_id | BIGINT | 대여자 회원 ID | 논리 FK → member-service.MEMBER.member_id |
| product_id | BIGINT | 상품 ID | 논리 FK → product-service.PRODUCT.product_id |
| rental_amount | DECIMAL(18,2) | 해당 주문 렌탈 금액 | - |
| fee_amount | DECIMAL(18,2) | 수수료 금액 | - |
| created_at | DATETIME | 생성 일시 | - |

## 📝 프로젝트 기능정의서
### 1. 회원·인증
- 회원가입(이메일/비밀번호, 해시 저장), 기본 ROLE: USER
- 소셜 로그인(OAuth+JWT), 이메일 인증
- 역할: USER / SELLER(추가 등록) / ADMIN
- 예치금 관리: 잔액 조회·충전(PG), 내역 조회
- 회원 정보 관리: 프로필 수정, 비밀번호 변경, 탈퇴

### 2. 예치금 & 결제
- 예치금 충전(PG 연동: 토스/카카오페이/아임포트 등)
- 렌탈 결제: 장바구니 기반, 잔액 부족 시 충전 유도, 결제 후 상태 `PAID`
- 취소/환불: 기간 기준 부분·전체 환불, 예치금 반환, 취소 수수료 차감
- 민감 정보 암호화 저장 및 로그 마스킹

### 3. 상품 & 장바구니
- 상품 목록/상세 조회, 공개 여부 반영
- ElasticSearch 검색/필터: 카테고리, 태그, 키워드, 판매자, 기간(대여 가능 여부)
- 장바구니 담기/수정/삭제, 장바구니 → 예치금 결제

### 4. 렌탈 도메인
- 렌탈 신청: 기간 입력 → 금액 계산 → 예치금 결제 → 상태 `REQUESTED`
- 상태 흐름: `REQUESTED → PAID → RENTING → RETURN_REQUESTED → RETURNED → COMPLETED`
- 렌탈 연장: 추가 금액 계산 후 재결제 및 기간 업데이트
- 반납/파손 처리: 반납 요청·승인, 파손/연체 시 추가 요금(예치금 차감)
- Kafka 이벤트: 상태 변경 시 발행, 알림/정산 서비스 소비

### 5. 판매자 & 정산
- 판매자 등록(일반 회원 → 판매자 전환, 추가 정보 입력)
- 판매자 정보 관리(프로필 조회/수정, 상태 관리)
- 판매자 상품 관리(등록/수정/삭제/숨김)
- 판매자 문의(사용자↔판매자 1:1 채팅, 선택적)
- 월 정산: 월 기준 매출 합산 → 수수료 차감 → 정산 금액 계산 → 정산 내역 조회(월/상품/렌탈별)

---

**마지막 업데이트** | 2025-09-24 | settlement 도메인 README 정리 최초 등록
