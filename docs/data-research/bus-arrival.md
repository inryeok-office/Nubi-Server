# 광주 버스 실시간 도착정보

## 공식 데이터

- 공식 데이터명: 전남광주통합특별시_광주버스정보의 BIS 도착 정보
- 제공기관: 전남광주통합특별시 대중교통과
- 공식 URL: <https://www.data.go.kr/data/15157923/openapi.do>
- 형식: REST, JSON+XML
- 인증: 공공데이터포털 서비스키 필요

## 실제 공식 사이트 응답

```text
GET https://bus.gwangju.go.kr/busmap/lineStationArriveInfoListTemp2?BUSSTOP_ID=2607
```

관찰된 주요 필드:

| 필드 | 의미 |
| --- | --- |
| `BUS_ID` | 현재 도착 예정 차량의 차량 식별자 |
| `LOW_BUS` | 저상버스 여부로 관찰됨: `1` 저상, `0` 일반 |
| `LINE_ID`, `LINE_NAME`, `LINE_ENG` | 노선 식별자 및 표시명 |
| `BUSSTOP_NAME`, `CURR_STOP_ID` | 현재 위치 정류소 |
| `REMAIN_MIN`, `REMAIN_STOP` | 도착 예상 분·남은 정류소 수 |
| `ARRIVE_FLAG`, `VALID` | 임박 및 유효 상태 코드 |
| `DIR_START`, `DIR_END` | 운행 방향 |

샘플은 [`samples/bus-arrival-2607.json`](samples/bus-arrival-2607.json)에 저장했다. 응답에는 저상(`LOW_BUS=1`)과 일반(`LOW_BUS=0`) 차량이 함께 나타난다.

## 데이터 갱신·호출 제한

- 공식 OpenAPI 상세 페이지는 실시간 도착정보 제공을 설명하지만, 이 조사에서는 인증키가 없어 실제 포털 API의 갱신 주기와 호출 제한을 실행 검증하지 못했다.
- 공식 포털 페이지에 표시된 개발계정 신청 가능 트래픽은 일 100회이다.
- 도착 예상값은 조회 시점의 값이다. 사용자에게 안내할 때 조회 시각, 데이터 지연 및 운행 취소를 함께 표시해야 한다.

