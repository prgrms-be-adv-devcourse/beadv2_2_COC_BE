리뷰 요약 변경사항 (버킷 방식)
===========================

개요
----
리뷰 요약 로직(버킷 요약) 변경과 관련 API 동작 변경을 정리한 문서입니다.
SQL/마이그레이션 단계는 제외했습니다.
리뷰 작성 가능 기간은 `review.reviewable-window` 설정으로 제어됩니다. (기본값: 7d)

요약 흐름 (신규)
---------------
- 신규 리뷰가 `b`(min-new-count) 개 누적될 때마다 버킷 요약이 생성됩니다.
- 최초 버킷은 총 리뷰 수가 `a`(min-total-count) 이상일 때 생성됩니다.
- 스케줄러는 설정된 cron 시간에만 최종 요약을 갱신합니다.
- 최종 요약은 최근 `c`개의 리뷰를 커버하는 최신 버킷들로 합성됩니다.
- 최종 요약은 `review_summary`, 버킷 요약은 `review_summary_bucket`에 저장됩니다.
- `review_summary.total_review_count`는 리뷰 생성/삭제 시 갱신됩니다.
- `review_summary.rating_sum`은 리뷰 생성/수정/삭제 시 갱신됩니다.

핵심 동작
---------
- 최초 요약은 `a`개 리뷰가 쌓이면 생성됩니다.
- 이후 요약은 `b`개 리뷰가 추가될 때마다 갱신됩니다.
- 마지막 버킷 이후 신규 리뷰가 `b`개 미만이면 새 버킷/최종 요약이 생성되지 않습니다.
- 최종 요약은 최근 `c`개 리뷰 범위를 만족하는 최신 버킷들만 사용합니다.

API 변경 사항
------------
리뷰 목록
- `GET /api/reviews?sellerId={sellerId}` 응답의 `ReviewListResponse`에 `content` 포함
- `GET /api/reviews/me` 응답의 `ReviewListResponse`에 `content` 포함
- `GET /api/reviews`와 `GET /api/reviews/me`는 `rating=1~5` 필터 지원
- `GET /api/reviews`와 `GET /api/reviews/me`는 `sort` 파라미터로 정렬 지원
  - 예: `sort=createdAt,desc`(최신순), `sort=createdAt,asc`(오래된순)
  - 예: `sort=rating,desc`(별점 높은순), `sort=rating,asc`(별점 낮은순)

리뷰 상세
- `GET /api/reviews/{reviewId}` 제거됨

요약
- `GET /api/reviews/summary?sellerId={sellerId}` 응답 형태는 동일

ReviewSummaryResponse
---------------------
```
{
  "sellerId": 3,
  "summary": "text",
  "reviewCount": 4,
  "totalReviewCount": 10,
  "ratingSum": 45,
  "averageRating": 4.5,
  "summarizedAt": "2026-01-15T11:00:00"
}
```

ReviewListResponse
------------------
```
{
  "reviewId": 5,
  "rentalItemId": 3,
  "sellerId": 3,
  "memberId": 2,
  "rating": 5,
  "content": "text",
  "createdAt": "2026-01-15T16:28:25.069783"
}
```

메모
----
- `a`와 `b`를 동일하게 두면 버킷 크기를 단순화할 수 있습니다.
- `c`는 버킷 개수가 아니라 **리뷰 개수 기준**입니다.
