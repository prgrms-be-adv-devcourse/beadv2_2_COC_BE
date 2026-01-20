# 로그 표준화 가이드

본 문서는 서비스 로그를 일관된 규칙으로 남기기 위한 표준을 정의합니다. 로그 구조를 통일하면 Kibana에서 필드별 필터링/집계가 쉬워지고, 장애 분석 속도가 빨라집니다.

## 목표
- 모든 서비스가 동일한 로그 구조를 사용한다.
- 예외(에러) 로그는 일관된 필드로 기록한다.
- 요청/응답 로그는 별도의 식별자와 필드로 구분한다.

## 기본 원칙
- 로그는 JSON 구조로 출력한다.
- 키 이름은 소문자 + 점 표기(`error.code`)를 사용한다.
- 동일 의미의 값은 서비스마다 다른 키를 쓰지 않는다.
- 사용자 개인정보(PII)는 로그에 남기지 않는다.
- 예외 로그뿐 아니라 **일반 로그도 구조화 필드**로 남긴다.

## 표준 필드
### 공통 필드 (모든 로그)
- `@timestamp`: 로그 시각
- `level`: 로그 레벨 (`INFO`, `WARN`, `ERROR`)
- `message`: 사람이 읽는 메시지
- `logger_name`: 로거 이름
- `service`: 서비스 이름
- `env`: 환경 (dev/stage/prod)
- `trace_id`: 분산 추적 ID (가능하면)
- `span_id`: 분산 추적 Span ID (가능하면)

### 에러 로그 전용 필드
- `error.type`: 에러 유형 (예: `PRODUCT_NOT_FOUND`)
- `error.code`: 에러 코드 (예: `PRODUCT-404`)
- `error.message`: 에러 상세 메시지
- `error.stacktrace`: 스택트레이스

### 요청/응답 로그 전용 필드
- `http.method`: HTTP 메서드
- `http.path`: 요청 경로
- `http.status`: 응답 상태 코드
- `http.duration_ms`: 처리 시간(ms)
- `client.ip`: 클라이언트 IP
- `user.id`: 사용자 식별자 (가능하면)

## 로그 유형 구분 규칙
- `log_type=service`: 애플리케이션 비즈니스 로그
- `log_type=access`: API 요청/응답 로그
- `log_type=system`: 인프라/플랫폼 로그

## 에러 로그 작성 규칙
### 예외 처리 위치
- 각 서비스의 `GlobalExceptionHandler`(`@RestControllerAdvice`)에서 에러 로그를 출력한다.
- 예외 발생 시 `error.*` 필드를 반드시 포함한다.
- 단순 문자열 로그가 아니라 **구조화 필드**로 남긴다.

### 구조화 필드 구성(권장)
- `error.code`: 에러 코드
- `error.message`: 상세 메시지
- `error.type`: 에러 유형(코드 enum 이름)
- `http.status`: HTTP 상태 코드
- `request.path`: 요청 경로
- `exception.class`: 예외 클래스

### 에러 로그 예시
```json
{
  "level": "ERROR",
  "message": "unexpected_exception",
  "logger_name": "com.coc.product.exception.GlobalExceptionHandler",
  "service": "product-service",
  "env": "prod",
  "trace_id": "abc123",
  "span_id": "def456",
  "error.type": "INTERNAL_ERROR",
  "error.code": "COMMON-500",
  "error.message": "알 수 없는 오류가 발생했습니다.",
  "http.status": 500,
  "request.path": "/products/1",
  "exception.class": "java.lang.IllegalStateException",
  "error.stacktrace": "..."
}
```

### GlobalExceptionHandler 적용 방향
- 비즈니스 예외는 `log.warn("business_exception", ...)` 형태로 기록한다.
- 예상치 못한 예외는 `log.error("unexpected_exception", ...)` 형태로 기록한다.
- 공통 필드 + `error.*` + `http.status` + `request.path`를 구조화 필드로 남긴다.

## 요청/응답 로그 작성 규칙
- 게이트웨이에서 access log를 JSON으로 기록한다.
- `log_type=access`를 필수로 포함한다.

### access 로그 예시
```json
{
  "log_type": "access",
  "level": "INFO",
  "service": "modi-gateway",
  "trace_id": "abc123",
  "http.method": "GET",
  "http.path": "/product-service/api/products",
  "http.status": 200,
  "http.duration_ms": 42,
  "client.ip": "203.0.113.10",
  "user.id": "member-123"
}
```

## 금지 사항
- SQL 원문 전체를 그대로 로그로 남기지 않는다.
- 예외 메시지에 개인정보(이메일, 주민번호 등)를 포함하지 않는다.
- 동일한 의미의 필드를 서비스마다 다른 이름으로 사용하지 않는다.

## 권장 구현 방법
- `logstash-logback-encoder` 사용
- 공통 로깅 유틸 모듈(`common-logging`)에 에러 로깅 헬퍼 추가
- `@ControllerAdvice`에서 `error.*` 필드를 일관되게 채움

## 전체 로그 표준화 적용 방식
- 모든 서비스 로그는 `StructuredArguments.kv(...)`로 구조화 필드를 추가한다.
- 공통 필드(`service`, `env`, `trace_id`, `span_id`)는 MDC 또는 공통 유틸에서 자동 주입한다.
- 로그 메시지는 짧고 일정한 이벤트명으로 유지한다. (예: `product_created`, `order_cancelled`)
- 상세 정보는 구조화 필드로만 남긴다. (예: `product.id`, `order.id`)

### 일반 로그 예시
```json
{
  "level": "INFO",
  "message": "product_created",
  "service": "product-service",
  "trace_id": "abc123",
  "product.id": "P-1001",
  "product.name": "아이폰"
}
```

## 체크리스트
- [ ] 모든 서비스가 JSON 로그를 출력하는가?
- [ ] 모든 로그가 구조화 필드로 기록되는가?
- [ ] 에러 로그에 `error.*` 필드가 포함되는가?
- [ ] access log에 `log_type=access`가 포함되는가?
- [ ] `service`, `env`, `trace_id` 필드가 일관적으로 존재하는가?
