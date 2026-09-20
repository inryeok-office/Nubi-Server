# 광주 버스 실시간 위치정보

## 공식 데이터

- 공식 데이터명: 전남광주통합특별시_광주버스정보의 BIS 노선 버스위치정보
- 제공기관: 전남광주통합특별시 대중교통과
- 공식 URL: <https://www.data.go.kr/data/15157923/openapi.do>
- 형식: REST, JSON+XML
- 인증: 공공데이터포털 서비스키 필요

## 실제 공식 사이트 응답

```text
GET https://bus.gwangju.go.kr/busmap/lineBusLocationListTemp2?LINE_ID=1
GET https://bus.gwangju.go.kr/busmap/lineLowLocationListTemp2?LINE_ID=1
```

관찰된 주요 필드:

| 필드 | 의미 |
| --- | --- |
| `BUS_ID` | 운행 중인 차량 식별자 |
| `LOW_BUS` | 저상버스 여부로 관찰됨 |
| `LONGITUDE`, `LATITUDE` | 차량 위치 좌표 |
| `BUSSTOP_NAME`, `NEXT_BUSSTOP` | 현재/다음 정류소 표시값 |
| `CURR_BUSSTOP_SEQ`, `RETURN_SEQ` | 노선 내 현재·회차 순번 |

샘플은 [`samples/bus-position-line-1.json`](samples/bus-position-line-1.json)에 저장했다. 같은 노선 응답에서 `LOW_BUS=1`인 차량 여러 대와 `LOW_BUS=0`인 차량이 함께 관찰됐다.

## 해석상 주의사항

- 웹 사이트의 `*Temp2` 경로는 실제 운영 화면이 사용하는 응답이지만 공공데이터포털의 안정적인 외부 API 계약과 동일하다고 단정하지 않는다.
- `BUS_ID`와 `LOW_BUS`의 의미·유효기간·차량 교체 시 정책은 기관의 공식 스키마 확인이 필요하다.
- 위치 응답과 도착 응답을 서로 다른 시각에 조회하면 차량 운행 상태가 달라질 수 있으므로, 매칭 시 동일 수집 시각 또는 허용 시간창을 기록해야 한다.

