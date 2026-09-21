# 광주 버스 노선

## 공식 데이터

- 공식 데이터명: 전남광주통합특별시_광주버스정보
- 제공기관: 전남광주통합특별시 대중교통과
- 공식 URL: <https://www.data.go.kr/data/15157923/openapi.do>
- 형식: REST, JSON+XML
- 인증: 공공데이터포털 서비스키 필요. 개발계정 신청 가능 트래픽은 일 100회.

## 실제 공식 사이트 응답

공식 버스정보 사이트가 호출하는 경로:

```text
GET https://bus.gwangju.go.kr/busmap/lineSearchListTemp2
GET https://bus.gwangju.go.kr/busmap/lineSearchListTemp2?LOW_LINE=1
```

관찰된 주요 필드:

| 필드 | 의미 | 식별자 성격 |
| --- | --- | --- |
| `LINE_ID` | 노선 내부 식별자 | 노선 식별자 |
| `LINE_NAME` | 노선명 | 표시값 |
| `DIR_UP_NAME`, `DIR_DOWN_NAME` | 상·하행 종점/방향명 | 방향 보조값 |
| `LINE_KIND` | 노선 유형 코드 | 코드표 확인 필요 |
| `RUN_TIME` | 운행시간표 링크 표시값 | 링크/표시값 |

샘플 응답은 [`samples/bus-route-list.json`](samples/bus-route-list.json)에 저장했다.

## 품질 및 주의사항

- `LINE_ID`는 웹 응답에서 확인되지만, 공공데이터포털 참고문서의 공식 영속성 보장은 아직 확인하지 못했다.
- 노선명에는 순환·좌석·타 지역 노선이 함께 포함될 수 있으므로 광주 시내버스만 별도 필터링해야 한다.
- 노선 목록은 실시간 운영 상태가 아니라 기준정보이며, 차량의 현재 운행 여부는 도착/위치 응답으로 확인해야 한다.

