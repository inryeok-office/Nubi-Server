# NUBI Server MVP API 계약

실행 중인 애플리케이션의 `/swagger-ui.html` 또는 `/v3/api-docs`가 기계 판독 가능한
계약의 기준입니다.

## 정류소

- `GET /api/v1/stops/nearby`
  - 필수 query: `latitude`, `longitude`
  - 선택 query: `radiusMeters`(1~2,000, 기본 500), `limit`(1~100, 기본 20)
  - 결과는 PostGIS 거리순이며 `distanceMeters`는 미터입니다.
- `GET /api/v1/stops/{stopId}`
- `GET /api/v1/stops/{stopId}/arrivals`

도착정보는 조회 시점의 스냅샷입니다. `dataSource`는 `observed-web-api` 또는
`official-open-api`이고, 현재 기본값은 전자입니다.

## 노선

- `GET /api/v1/routes/{routeId}`
- `GET /api/v1/routes/{routeId}/stops`

노선·정류소 API는 collector가 PostGIS에 적재한 정적 데이터에서 응답합니다.
데이터가 없으면 `404`입니다.

## 오류

```json
{
  "code": "INVALID_REQUEST",
  "message": "radiusMeters must be between 1 and 2000",
  "timestamp": "2026-09-21T00:00:00Z"
}
```

- `400 INVALID_REQUEST`: 입력값 오류
- `404 NOT_FOUND`: DB에 없는 노선·정류소
- `502 UPSTREAM_ERROR`: 실시간 원천 조회 실패

## 저상버스 상태

API는 외부 필드명 `LOW_BUS`를 그대로 노출하지 않고 `lowFloorStatus`로 변환합니다.

| API 값 | 관찰 원천 값 | 의미 |
| --- | --- | --- |
| `low-floor` | `1` | 저상으로 관찰됨 |
| `standard` | `0` | 일반으로 관찰됨 |
| `unknown` | 누락·공백·그 외 | 기관 코드 확인 전 미확정 |

이 상태는 차량 응답의 관찰값이며 휠체어 탑승 가능이나 운행 지속을 보장하지 않습니다.
