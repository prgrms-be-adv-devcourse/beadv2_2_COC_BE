# 게이트웨이 권한 조회/캐시 설계 (JWT role 제거)

## 목적
- JWT에서 `role` 클레임을 제거하고, 게이트웨이가 member-service에서 권한을 조회/캐싱한다.
- 다운그레이드(SELLER -> MEMBER) 리스크를 줄이기 위해 `fresh/stale` 정책을 적용한다.

## 요청 흐름
1. 게이트웨이가 JWT 서명을 검증하고 `memberId(sub)`만 추출한다.
2. 게이트웨이가 member-service 내부 API에서 권한을 조회한다.
3. 조회 결과를 캐시에 저장하고, 요청에 필요한 권한을 검사한다.
4. 통과 시 게이트웨이가 `X-Member-Id`, `X-Roles` 헤더를 붙여 downstream으로 전달한다.

## 내부 권한 조회 API
- 요청: `GET /internal/members/{memberId}/authz`
- 응답 예:
```json
{
  "memberId": 1,
  "roles": ["MEMBER", "SELLER"]
}
```
- 내부 호출은 `X-Internal-Token` 헤더를 사용한다.

## 캐시/갱신 정책
- 기본 TTL: 2분
- stale window: 3분
- stale 허용: 기본적으로 `GET`만 허용
- fresh-only 경로: 특정 민감 경로는 stale 허용하지 않음
  - 예: `/seller-service/api/sellers/self`, `/seller-service/api/settlements/sellers/self/**` 등

## 장애/실패 정책
- 캐시가 있으면 정책에 따라 stale 사용 가능
- 캐시가 없고 조회 실패 시: **권한 필요한 요청은 거부 (fail-closed)**

## Kafka 기반 무효화
- member-service에서 역할 변경 시 `member-role-changed` 이벤트 발행
- 게이트웨이는 해당 이벤트를 소비하여 사용자 캐시를 즉시 제거
- 다음 요청 시 fresh 권한을 다시 조회함

## 설정값 (gateway)
```yaml
member-service:
  url: http://member-service:8085

internal:
  api:
    header: X-Internal-Token
    token: ${INTERNAL_API_TOKEN:}

gateway:
  authz:
    cache-ttl: 2m
    stale-window: 3m
    fetch-timeout: 2s
    kafka-group-id: modi-gateway-authz
    stale-allowed-methods:
      - GET
    fresh-only-paths:
      - /seller-service/api/sellers/products/{productId}
      - /seller-service/api/sellers/self
      - /seller-service/api/sellers/self/rentals
      - /seller-service/api/settlements/sellers/self
      - /seller-service/api/settlements/sellers/self/{sellerSettlementId}
      - /seller-service/api/settlements/sellers/self/{sellerSettlementId}/lines
```

## 관련 구현 위치
- 게이트웨이 캐시/조회: `modi-gateway/src/main/java/com/coc/gateway/security/authz/MemberAuthzService.java`
- 내부 호출 클라이언트: `modi-gateway/src/main/java/com/coc/gateway/security/authz/MemberAuthzClient.java`
- 정책 설정: `modi-gateway/src/main/java/com/coc/gateway/security/authz/AuthzCacheProperties.java`
- 인증 흐름 연결: `modi-gateway/src/main/java/com/coc/gateway/config/WebFluxSecurityConfig.java`
- 헤더 주입: `modi-gateway/src/main/java/com/coc/gateway/security/GlobalAuthHeaderFilter.java`
- 이벤트 소비: `modi-gateway/src/main/java/com/coc/gateway/security/authz/MemberRoleChangedListener.java`

## 참고
- downstream 서비스는 `X-Member-Id`, `X-Roles` 헤더로 인증/인가를 처리한다.
- 새 민감 API를 추가하면 `fresh-only-paths`에 반영해야 한다.
