# DB ERD (요약)

## 공통
- 서비스별로 독립 스키마를 사용합니다.
- 대부분의 테이블은 `BaseEntity`를 상속하여 `created_at`, `updated_at` 컬럼을 포함합니다.
- 일부 테이블은 외부 서비스의 ID를 참조하지만 FK 제약은 두지 않습니다. (논리적 참조)

## member 스키마
```mermaid
erDiagram
  MEMBER {
    bigint id PK
    varchar email
    varchar name
    varchar status
    varchar password
    varchar role
    varchar phone
    varchar provider
    varchar provider_id
  }

  ADDRESS {
    bigint id PK
    bigint member_id FK
    varchar address_label
    varchar recipient_name
    varchar recipient_phone
    varchar type
    varchar postcode
    varchar road_address
    varchar jibun_address
    varchar detail_address
    boolean is_default
  }

  MEMBER_OUTBOX {
    uuid id PK
    varchar aggregate_type
    bigint aggregate_id
    varchar event_type
    text payload
    varchar status
    int retry_count
    datetime processed_at
    varchar last_error
  }

  MEMBER ||--o{ ADDRESS : has
```

## account 스키마
```mermaid
erDiagram
  MEMBER_WALLET {
    bigint id PK
    bigint member_id
    decimal balance
    bigint version
  }

  WALLET_TRANSACTION {
    bigint id PK
    bigint wallet_id FK
    bigint member_id
    varchar tx_type
    decimal amount
    decimal balance_after
    bigint pg_deposit_id FK
    bigint related_rental_id
    bigint related_rental_item_id
    bigint related_settlement_id
    varchar description
    varchar payment_key
    varchar request_id
  }

  PG_DEPOSIT {
    bigint id PK
    bigint member_id
    decimal amount
    varchar status
    varchar pg_provider
    varchar pg_tid
    varchar payment_key
    datetime requested_at
    datetime approved_at
    varchar failed_reason
  }

  MEMBER_WALLET ||--o{ WALLET_TRANSACTION : has
  PG_DEPOSIT ||--o{ WALLET_TRANSACTION : used_by
```

## product 스키마
```mermaid
erDiagram
  PRODUCT {
    bigint id PK
    bigint seller_id
    varchar name
    varchar description
    decimal price_per_day
    decimal security_deposit_amount
    varchar status
    varchar category
    bigint thumbnail_image_id
    jsonb specs
  }

  PRODUCT_IMAGE {
    bigint id PK
    bigint product_id FK
    varchar url
    int ordering
  }

  PRODUCT_VIEW_LOG {
    bigint id PK
    bigint product_id
    bigint member_id
    date view_date
  }

  PRODUCT_VIEW_DAILY {
    date view_date PK
    bigint product_id PK
    bigint view_count
  }

  PRODUCT_SEARCH_LOG {
    bigint id PK
    bigint member_id
    varchar keyword
    varchar category
    decimal min_price
    decimal max_price
    bigint seller_id
    date start_date
    date end_date
    varchar sort_type
    varchar cursor
    int size
  }

  PRODUCT ||--o{ PRODUCT_IMAGE : has
```

## rental 스키마
```mermaid
erDiagram
  RENTAL {
    bigint id PK
    bigint member_id
    varchar status
    decimal total_amount
    datetime paid_at
  }

  RENTAL_ITEM {
    bigint id PK
    bigint rental_id FK
    bigint product_id
    bigint seller_id
    date start_date
    date end_date
    varchar status
    decimal unit_price
    decimal security_deposit_amount
    datetime returned_at
    datetime canceled_at
  }

  RENTAL_EXTEND {
    bigint id PK
    bigint rental_item_id FK
    date old_end_date
    date new_end_date
    int extra_days
    decimal extra_amount
  }

  RENTAL_EVENT_LOG {
    bigint id PK
    bigint rental_id FK
    varchar event_type
    text payload_json
    datetime created_at
  }

  RENTAL_OUTBOX {
    uuid id PK
    varchar aggregate_type
    bigint aggregate_id
    varchar event_type
    text payload
    varchar status
    int retry_count
    datetime processed_at
    varchar last_error
  }

  RENTAL ||--o{ RENTAL_ITEM : has
  RENTAL_ITEM ||--o{ RENTAL_EXTEND : extends
  RENTAL ||--o{ RENTAL_EVENT_LOG : logs
```

## review 스키마
```mermaid
erDiagram
  REVIEW {
    bigint id PK
    bigint rental_item_id
    bigint seller_id
    bigint member_id
    smallint rating
    text content
    varchar status
  }

  REVIEW_OUTBOX {
    uuid id PK
    varchar aggregate_type
    bigint aggregate_id
    varchar event_type
    text payload
    varchar status
    int retry_count
    datetime processed_at
    varchar last_error
  }
```

## seller 스키마
```mermaid
erDiagram
  SELLER {
    bigint id PK
    bigint member_id
    varchar store_name
    varchar biz_reg_no
    varchar store_phone
    varchar status
  }

  SETTLEMENT_BATCH {
    bigint id PK
    varchar period_ym
    varchar status
    datetime started_at
    datetime completed_at
  }

  SETTLEMENT_BATCH_EXECUTION {
    bigint id PK
    bigint batch_id FK
    varchar batch_type
    text params
    varchar status
    datetime started_at
    datetime ended_at
    bigint duration_ms
    int total_count
    int success_count
    int fail_count
    decimal total_amount
    decimal fee_amount
    varchar last_cursor
    varchar error_message
  }

  SELLER_SETTLEMENT {
    bigint id PK
    bigint batch_id FK
    bigint seller_id
    varchar period_ym
    decimal total_rental_amount
    decimal total_fee_amount
    decimal settlement_amount
    varchar status
    datetime paid_at
  }

  SELLER_SETTLEMENT_LINE {
    bigint id PK
    bigint seller_settlement_id FK
    bigint seller_id
    bigint rental_item_id
    bigint member_id
    bigint product_id
    decimal rental_amount
    decimal fee_amount
  }

  CHAT_ROOM {
    bigint id PK
    varchar room_key
  }

  CHAT_PARTICIPANT {
    bigint id PK
    bigint room_id FK
    bigint member_id
    varchar role
    bigint last_read_message_id
    datetime last_read_at
  }

  CHAT_MESSAGE {
    bigint id PK
    bigint room_id FK
    bigint sender_id
    varchar sender_role
    varchar content
    datetime sent_at
    datetime read_at
  }

  SETTLEMENT_BATCH ||--o{ SETTLEMENT_BATCH_EXECUTION : runs
  SETTLEMENT_BATCH ||--o{ SELLER_SETTLEMENT : groups
  SELLER_SETTLEMENT ||--o{ SELLER_SETTLEMENT_LINE : has
  CHAT_ROOM ||--o{ CHAT_PARTICIPANT : joins
  CHAT_ROOM ||--o{ CHAT_MESSAGE : contains
```

## delivery 스키마
```mermaid
erDiagram
  DELIVERY {
    bigint id PK
    bigint rental_item_id
    varchar carrier_code
    varchar tracking_number
    varchar status
    varchar status_raw
    datetime last_tracked_at
  }
```

## notification 스키마
```mermaid
erDiagram
  NOTIFICATION {
    bigint id PK
    bigint receiver_id
    varchar type
    varchar title
    varchar content
    varchar reference_type
    bigint reference_id
    boolean read
  }
```
