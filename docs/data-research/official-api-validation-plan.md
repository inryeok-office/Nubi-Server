# 광주버스 정식 OpenAPI 검증 계획

작성 기준일: 2026-09-21 (Asia/Seoul)

이 문서는 공공데이터포털의 `전남광주통합특별시_광주버스정보` 정식 OpenAPI와
기존 `observed-web-api` 응답을 비교하기 위한 실행 계획이다. 이번 단계에서는
서비스키를 사용한 호출이나 official adapter 구현을 수행하지 않는다.

## 목적

observed-web-api와 official-open-api가 동일한 의미의 데이터를 제공하는지 검증한다.
특히 정식 API만으로 현재 MVP의 차량별 저상버스 식별이 유지되는지 확인한다.

## 공식 문서 확인 범위

공식 출처:

- [전남광주통합특별시_광주버스정보 OpenAPI](https://www.data.go.kr/data/15157923/openapi.do)
- 문서에 표시된 `OPENAPI활용가이드_광주버스 v1.0.docx`

공식 Swagger 명세에 표시된 API host는 `apis.data.go.kr/6290000/gj_bis`이며,
요청 방식은 모두 `GET`이다. 서비스키는 `serviceKey` query parameter로 전달하고,
응답 형식은 필수 `resultType` query parameter로 지정한다. 문서상 제공 형식은
JSON+XML이다.

## 공식 endpoint 및 요청 명세

| 기능 | 공식 endpoint | 필수 parameter | 주요 응답 식별자·필드 | 페이지네이션 | LOW_BUS / BUS_ID |
| --- | --- | --- | --- | --- | --- |
| 노선 조회 | `https://apis.data.go.kr/6290000/gj_bis/lineInfo` | `serviceKey`, `resultType` | `LINE_ID`, `LINE_NAME`, `DIR_UP_NAME`, `DIR_DOWN_NAME`, `FIRST_RUN_TIME`, `LAST_RUN_TIME`, `RUN_INTERVAL`, `LINE_KIND` | Swagger상 `pageNo`·`numOfRows` 없음. `ROW_COUNT`는 응답에 있음. 별도 페이지 정책은 미확인 | `LOW_BUS`: 없음, `BUS_ID`: 없음 |
| 정류소 조회 | `https://apis.data.go.kr/6290000/gj_bis/stationInfo` | `serviceKey`, `resultType` | `BUSSTOP_ID`, `BUSSTOP_NAME`, `NAME_E`, `LONGITUDE`, `LATITUDE`, `ARS_ID`, `NEXT_BUSSTOP` | Swagger상 `pageNo`·`numOfRows` 없음. `ROW_COUNT`는 응답에 있음. 별도 페이지 정책은 미확인 | `LOW_BUS`: 없음, `BUS_ID`: 없음 |
| 노선별 정류소 | `https://apis.data.go.kr/6290000/gj_bis/lineStationInfo` | `serviceKey`, `LINE_ID`, `resultType` | `LINE_ID`, `LINE_NAME`, `BUSSTOP_ID`, `BUSSTOP_NAME`, `ARS_ID`, `LONGITUDE`, `LATITUDE`, `RETURN_FLAG`, `SEQ` | Swagger상 `pageNo`·`numOfRows` 없음. `ROW_COUNT`는 응답에 있음. 별도 페이지 정책은 미확인 | `LOW_BUS`: 없음, `BUS_ID`: 없음 |
| 정류소 도착정보 | `https://apis.data.go.kr/6290000/gj_bis/arriveInfo` | `serviceKey`, `BUSSTOP_ID`, `resultType` | `LINE_ID`, `LINE_NAME`, `SHORT_LINE_NAME`, `BUS_ID`, `CURR_STOP_ID`, `BUSSTOP_NAME`, `REMAIN_MIN`, `REMAIN_STOP`, `DIR_START`, `DIR_END`, `LOW_BUS`, `ARRIVE_FLAG`, `LINE_KIND` | Swagger상 `pageNo`·`numOfRows` 없음. `ROW_COUNT`는 응답에 있음. 별도 페이지 정책은 미확인 | `BUS_ID`, `LOW_BUS` 모두 문서화됨 |
| 노선별 버스 위치 | `https://apis.data.go.kr/6290000/gj_bis/busLocationInfo` | `serviceKey`, `LINE_ID`, `resultType` | `LINE_ID`, `BUS_ID`, `CURR_STOP_ID`, `CARNO`, `LOW_BUS`, `SEQ` | Swagger상 `pageNo`·`numOfRows` 없음. `ROW_COUNT`는 응답에 있음. 별도 페이지 정책은 미확인 | `BUS_ID`, `LOW_BUS` 모두 문서화됨 |

정식 문서에서 확인된 `LOW_BUS` 설명은 버스 위치 응답 기준 `0: 일반, 1: 저상`이다.
도착 응답에도 필드가 존재하지만, 두 기능의 값 의미가 완전히 동일한지는 실제 응답
비교로 확인한다. 정식 위치 응답 스키마에는 `LONGITUDE`·`LATITUDE`가 문서화되어
있지 않으므로, 관찰 웹 위치 응답의 좌표와 동일하다고 가정하지 않는다.

정식 응답은 기존 observed 응답의 `list` envelope가 아니라 문서상
`RESPONSE.RESULT` 아래 기능별 목록(`LINE_LIST`, `STATION_LIST`, `BUSSTOP_LIST`,
`ARRIVE_LIST`, `BUSLOCATION_LIST`)과 `ITEM` 구조를 사용한다. 실제 JSON에서의
wrapper·빈 목록·오류 응답 형태는 서비스키 입력 후 확인 대상이다.

## 공식 오류 코드

공식 페이지에 표시된 오류 코드는 다음과 같다.

| 코드 | 명칭 | 검증 시 처리 |
| --- | --- | --- |
| 01 | `APPLICATION_ERROR` | 재시도 전 원문과 시각 기록 |
| 04 | `HTTP_ERROR` | endpoint·HTTP 응답 확인 |
| 05 | `SERVICETIMEOUT_ERROR` | timeout과 재시도 정책 별도 기록 |
| 10 | `INVALID_REQUEST_PARAMETER_ERROR` | parameter 이름·형식 확인 |
| 12 | `NO_OPENAPI_SERVICE_ERROR` | 공식 URL·서비스 폐기 여부 확인 |
| 20 | `SERVICE_KEY_IS_NULL` / `PERMISSION_DENIED` | 키 누락 또는 활용신청 권한 구분 |
| 22 | `LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR` | 일일 호출량 초과 |
| 23 | `LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR` | 초당 호출량 초과 |
| 29 | `BLACKLIST_IP_ACCESS_ERROR` | 호출 IP 확인 |
| 30 | `SERVICE_KEY_IS_NOT_REGISTERED_ERROR` | 키 등록·활용신청 상태 확인 |
| 31 | `DEADLINE_HAS_EXPIRED_ERROR` | 키 사용기간 확인 |

오류 body의 실제 JSON/XML wrapper와 HTTP status 조합은 서비스키 입력 후 확인한다.

## 검증 대상

### A. 노선

비교 대상:

- `LINE_ID`
- `LINE_NAME`
- 방향 정보: `DIR_UP_NAME`, `DIR_DOWN_NAME`
- 노선 유형: 공식 `LINE_KIND`, observed `LINE_KIND`

정식 응답은 운영시간·배차간격(`FIRST_RUN_TIME`, `LAST_RUN_TIME`, `RUN_INTERVAL`)도
제공하므로 observed 응답에 없을 때는 “official에만 존재”로 기록한다.

### B. 정류소

비교 대상:

- `BUSSTOP_ID`
- `ARS_ID`
- 명칭
- 위도·경도: `LATITUDE`, `LONGITUDE`

정식 `stationInfo`의 `BUSSTOP_NAME`과 observed의 `BUSSTOP_NAME`/`BUSSTOP_NM`을
정규화한 후 비교한다.

### C. 노선별 정류소

비교 대상:

- `LINE_ID`
- `BUSSTOP_ID`
- 정류장 순서: 정식 `SEQ`, observed 응답 배열 순서
- 좌표

정식 API의 `SEQ`를 기준값으로 사용하고, observed 배열 순서와 일치하는지 확인한다.
`RETURN_FLAG`는 정식에만 존재하면 추가 정보로 기록한다.

### D. 실시간 도착

가장 중요한 비교 대상이다.

비교 대상:

- `BUS_ID`
- `LINE_ID`
- 요청 정류소 `BUSSTOP_ID`와 응답의 관련 정류소 식별자
- `REMAIN_MIN`
- `REMAIN_STOP`
- `LOW_BUS`
- 데이터 생성·조회 시각 또는 그에 준하는 기준시각

정식 `arriveInfo` 명세상 요청에는 `BUSSTOP_ID`가 있으나 응답 필드 목록에는
`BUSSTOP_ID`가 별도로 표시되지 않고 `CURR_STOP_ID`와 `BUSSTOP_NAME`이 있다.
따라서 요청 정류소와 응답 정류소의 관계를 실제 응답으로 확인한다.

### E. 실시간 위치

비교 대상:

- `BUS_ID`
- `LINE_ID`
- `LOW_BUS`
- 위도·경도
- 현재 정류소 또는 순번: 정식 `CURR_STOP_ID`, `SEQ`

정식 `busLocationInfo` 명세에는 `BUS_ID`, `LINE_ID`, `CURR_STOP_ID`, `CARNO`,
`LOW_BUS`, `SEQ`가 있으나 위도·경도 필드는 없다. 따라서 좌표는 정식 API에서
제공되지 않는 것으로 우선 표시하고, 실제 응답에 undocumented 필드가 있는지는
검증 때 별도로 기록한다.

## 핵심 검증 질문

1. official API에서도 `BUS_ID`가 제공되는가?
2. official API에서도 차량별 `LOW_BUS`가 제공되는가?
3. `LOW_BUS=1/0`의 의미가 공식 문서와 실제 응답에서 일치하는가?
4. observed-web-api와 official-open-api의 `BUS_ID`가 동일한 차량을 가리키는가?
5. 동일 노선·정류소·시간대 조회 시 두 API의 결과가 일관적인가?
6. `BUS_ID`는 시간 경과 후에도 동일 차량에 안정적으로 유지되는가?
7. 예비차·차량교체 상황에서 `BUS_ID`/`LOW_BUS`가 어떻게 변하는가?
8. 데이터 생성시각 또는 기준시각 필드가 있는가?
9. 호출 제한을 고려했을 때 실시간 서비스에 사용할 수 있는가?
10. observed-web-api를 제거하고 official-open-api만으로 Server MVP를 유지할 수 있는가?

## 실제 검증 순서

서비스키를 로컬 환경변수에 입력한 뒤 다음 순서로 실행한다. 이번 단계에서는
이 순서를 문서화만 하며 실제 호출하지 않는다.

### Step 1 — 공식 API 인증 성공 여부 확인

`lineInfo`에 `serviceKey`와 `resultType=json`을 넣어 호출한다. 성공 응답과
인증 오류 응답 모두 serviceKey를 포함하지 않은 sanitized artifact로 기록한다.

### Step 2 — 노선 1개 선택

기존 조사에서 사용한 `LINE_ID=1` 또는 정식 `lineInfo`에서 대응되는 식별자를
우선 사용한다. 대응되지 않으면 임의 매칭하지 않고 차이로 기록한다.

### Step 3 — 해당 노선의 정류소 목록 조회

`lineStationInfo?LINE_ID={LINE_ID}`를 호출하고 정식 `SEQ`를 기준으로 정렬한다.

### Step 4 — 정류소 1~3개 선택

기존 조사 정류소 `BUSSTOP_ID=2607`을 우선 시도하되, 정식 노선 응답에 없으면
정식 응답에서 확인된 정류소를 선택하고 대응 관계를 기록한다.

### Step 5 — official 도착정보 조회

선택한 `BUSSTOP_ID`에 대해 `arriveInfo`를 호출하고 `BUS_ID`, `LINE_ID`,
`REMAIN_MIN`, `REMAIN_STOP`, `LOW_BUS`, 원문 응답 시각을 저장한다.

### Step 6 — 같은 시각에 observed-web-api 조회

동일 정류소에 기존 observed endpoint를 호출한다. 두 호출 사이의 실제 시간 차이를
밀리초 단위로 기록하며, 시간 차이가 큰 샘플은 차량 동일성 판정에서 제외한다.

### Step 7 — BUS_ID / LOW_BUS / 도착시간 비교

`BUS_ID`를 1차 key로 normalize하여 차량별로 `LOW_BUS`, `REMAIN_MIN`,
`REMAIN_STOP`을 비교한다. 한쪽에만 존재하는 차량은 불일치가 아니라 조회 시점 차이로
분류할 수 있도록 별도 상태를 둔다.

### Step 8 — official 위치 API와 observed 위치 API 비교

동일 `LINE_ID`에 대해 `busLocationInfo`와 observed 위치 endpoint를 비교한다.
정식 문서에는 좌표가 없으므로 `CURR_STOP_ID`·`SEQ`·`BUS_ID`·`LOW_BUS`를 우선
비교하고, 좌표 비교는 실제 필드가 확인될 때만 수행한다.

### Step 9 — 10~30분 동안 반복 조회

동일한 1~3개 정류소와 노선을 과도한 호출 없이 반복 조회한다. 호출 시각, 결과 행 수,
차량 출현·소실, `LOW_BUS` 변경, 오류 코드, 응답 지연을 기록한다. 개발계정의
공식 페이지 표시 호출 한도는 일 100회이므로 검증 횟수를 사전에 계산한다.

### Step 10 — 결과 판정

- **A1**: official API만으로 차량별 저상버스 식별 가능
- **A2**: official API와 observed API를 함께 써야 함
- **B**: 노선 단위까지만 가능
- **C**: 정식 API로는 핵심 기능 구현 불가

판정은 단일 응답이 아니라 반복 샘플과 오류·결측을 포함해 내린다.

## 검증 자동화 설계안

실제 구현은 다음 단계에서 별도 스크립트 또는 integration test로 진행한다.

```text
official response fetch
        ↓
observed response fetch
        ↓
source-specific normalize
        ↓
BUS_ID 기준 outer comparison
        ↓
LOW_BUS / 도착시간 / 위치 식별자 비교
        ↓
sanitized diff report
```

정규화 결과 예:

```text
vehicleId | officialLowBus | observedLowBus | match
5280      | 1               | 1              | true
```

자동화 원칙:

- official wrapper `RESPONSE.RESULT.*.ITEM`과 observed `list`를 먼저 정규화한다.
- `BUS_ID`는 문자열로 정규화하고 공백을 제거한다.
- 숫자 필드는 파싱 실패를 `null`로 두며 임의의 0으로 바꾸지 않는다.
- 조회 시각과 호출 간격을 결과에 포함한다.
- 비교 결과, 로그, fixture에 serviceKey를 절대 포함하지 않는다.
- raw response를 저장할 때도 query string의 `serviceKey`를 제거한다.
- 키가 포함된 URL은 출력·예외·CI artifact에 남기지 않는다.

## 필요한 사용자 입력

1. 루트 `.env`의 `GWANGJU_BUS_API_KEY=` 뒤에 서비스키를 직접 입력한다.
2. `.env`는 `.gitignore` 대상이므로 commit·PR에 포함하지 않는다.
3. API Key를 채팅, GitHub Issue, PR, commit, 로그에 넣지 않는다.
4. 입력을 완료하면 Codex에 `키 입력 완료`라고 알린다.

키 입력 전에는 실제 API 호출을 수행하지 않는다.

## `.env` 로딩 방법

현재 Spring Boot의 `application.yml`은 `GWANGJU_BUS_API_KEY` 환경변수를 읽지만,
Spring Boot와 `./gradlew bootRun`은 저장소 루트의 `.env`를 자동으로 읽지 않는다.
Docker Compose에도 현재 이 키를 위한 `env_file` 설정은 없다. 따라서 dotenv 라이브러리는
추가하지 않는다.

가장 단순한 실행 방법은 IDE Run Configuration 또는 실행 중인 shell의 환경변수에
`GWANGJU_BUS_API_KEY`를 설정하는 것이다. PowerShell에서는 키를 화면에 출력하지 않는
방식으로 현재 세션에만 설정한 뒤 같은 세션에서 `./gradlew bootRun`을 실행한다.
Bash 계열도 같은 방식으로 현재 shell에만 `export GWANGJU_BUS_API_KEY=...`를 적용한다.
루트 `.env`는 입력값 보관용 placeholder 파일이며 자동 로딩 파일이 아니다.

## 다음 단계에서 실행할 검증

- 인증 성공 여부와 오류 body 확인
- 정식 5개 endpoint의 실제 JSON/XML wrapper 확인
- 정식 응답의 `BUS_ID`·`LOW_BUS` 필드와 observed 응답 비교
- 반복 샘플의 차량 식별자 안정성·결측·지연·호출 제한 측정
- A1/A2/B/C 판정 후에만 official adapter 구현 여부 결정

현재 단계에서는 기존 observed adapter, DB schema, public API 계약을 변경하지 않는다.
