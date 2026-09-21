# NUBI Server

광주광역시 교통약자 이동지원 및 이동권 분석 플랫폼의 Server MVP입니다.

현재 목표는 기관 문의 전 기술 검증입니다. 실시간 도착 응답의 차량 단위
`BUS_ID`와 `LOW_BUS`를 바탕으로 저상버스 상태를 표시할 수 있지만, 기관의
공식 코드 정의와 품질 보장이 확인되기 전에는 휠체어 탑승 가능을 보장하지 않습니다.

## 개발 환경

- JDK 21
- Kotlin / Spring Boot 3.5.16
- Gradle Kotlin DSL
- PostgreSQL 16 + PostGIS 3.4
- Docker Compose

## 실행

```bash
cp .env.example .env
docker compose up -d postgres
./gradlew bootRun
```

상태 확인: `http://localhost:8080/actuator/health`

Swagger UI: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 환경변수

`.env.example`을 복사해 로컬에서만 사용합니다. Secret과 서비스키는 커밋하지 않습니다.

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `GWANGJU_BUS_DATA_SOURCE` | `observed-web-api` | `observed-web-api` 또는 준비된 `official-open-api` 경계 |
| `GWANGJU_BUS_API_KEY` | 빈 값 | 공공데이터포털 정식 API 서비스키. 현재 계약 확인 전에는 사용하지 않음 |
| `GWANGJU_BUS_OBSERVED_BASE_URL` | `https://bus.gwangju.go.kr` | 조사된 운영 웹 응답 adapter의 base URL |
| `GWANGJU_BUS_OFFICIAL_BASE_URL` | 빈 값 | 기관 확인 후 정식 API adapter에 설정 |
| `GWANGJU_BUS_COLLECT_ON_STARTUP` | `false` | `true`일 때 정적 데이터 collector를 한 번 실행 |

`observed-web-api`는 공식 버스정보 사이트가 사용하는 응답을 기술 검증용으로
격리한 경로입니다. `*Temp2` endpoint를 안정적인 외부 계약으로 간주하지 않습니다.
정식 OpenAPI의 서비스키가 있어도 endpoint·파라미터 계약을 추측하지 않도록
현재 `official-open-api` adapter는 명시적으로 미구현 상태를 반환합니다.

## 공개 API

정적 노선·정류소 데이터는 DB에 수집된 후 조회할 수 있습니다.

- `GET /api/v1/stops/nearby?latitude={lat}&longitude={lon}&radiusMeters=500&limit=20`
- `GET /api/v1/stops/{stopId}`
- `GET /api/v1/stops/{stopId}/arrivals`
- `GET /api/v1/routes/{routeId}`
- `GET /api/v1/routes/{routeId}/stops`

도착정보 응답의 `lowFloorStatus`는 다음 세 값 중 하나입니다.

- `low-floor`: 원천 `LOW_BUS=1`
- `standard`: 원천 `LOW_BUS=0`
- `unknown`: 누락·공백·미확인 코드

이는 조회 시점의 원천 관찰 상태이며 휠체어 탑승 가능 보장이 아닙니다.
각 도착 응답에는 `dataSource`, `fetchedAt`, `observedAt`가 포함됩니다.

## 정적 데이터 수집

Collector는 기본 비활성입니다. 로컬 DB를 초기화하거나 최신화할 때만 다음처럼
명시적으로 실행합니다.

```powershell
$env:GWANGJU_BUS_COLLECT_ON_STARTUP = "true"
./gradlew bootRun
```

노선·정류소·노선-정류소 관계는 upsert 방식으로 저장되어 반복 실행해도 중복되지
않습니다. 노선 경유 순서는 관찰된 응답 배열의 순서를 사용하며, 공식 순서 필드의
의미는 기관 확인이 필요합니다.

## 검증

```bash
./gradlew test
./gradlew ktlintCheck
./gradlew bootJar
```

Testcontainers 기반 PostGIS 통합 테스트는 Docker가 실행 중이어야 합니다.

## 조사 근거

공식 출처와 실제 관찰 응답은 [`docs/data-research/`](docs/data-research/)에 기록되어
있습니다. 특히 [타당성 보고서](docs/data-research/feasibility-report.md)는
저상버스 차량 단위 식별 결론 A와 남은 기관 확인 항목의 기준 문서입니다.

이번 MVP에는 회원·인증, 경로탐색, 접근성 점수, 탑승 보장, Redis/Kafka/MSA,
기관용 분석 대시보드를 포함하지 않습니다.
