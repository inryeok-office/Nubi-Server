# 광주 버스 정류소 및 노선-정류소

## 공식 데이터

- 공식 데이터명: 전남광주통합특별시_광주버스정보
- 공식 URL: <https://www.data.go.kr/data/15157923/openapi.do>
- 별도 파일데이터로는 [전남광주통합특별시_정류소_20241231](https://www.data.go.kr/data/15128208/fileData.do)이 있으며 2024-12-31 기준 2,379개 정류소, ARS ID와 행정동 등을 제공한다.

## 정류소 검색 실제 응답

```text
GET https://bus.gwangju.go.kr/busmap/stationSearchListTemp2?BUSSTOP_NAME=
```

관찰된 주요 필드:

| 필드 | 의미 |
| --- | --- |
| `BUSSTOP_ID` | 공식 사이트 내부 정류소 식별자 |
| `ARS_ID` | 정류소 안내용 ARS 번호 |
| `BUSSTOP_NAME`, `BUSSTOP_NM` | 정류소명 및 방향 포함 표시명 |
| `NEXT_BUSSTOP` | 다음 정류소 표시값 |
| `LONGITUDE`, `LATITUDE` | 좌표 |

샘플은 [`samples/bus-stop-search.json`](samples/bus-stop-search.json)에 저장했다.

## 노선-정류소 실제 응답

```text
GET https://bus.gwangju.go.kr/busmap/lineStationListTemp2?LINE_ID=1
```

`LINE_ID`, `BUSSTOP_ID`, `ARS_ID`, `LINE_NAME`, 좌표가 함께 반환된다. 배열 순서가 노선 경유 순서를 나타내는 것으로 보이지만, 순서 필드의 공식 명세는 인증된 OpenAPI 참고문서에서 확인해야 한다.

샘플은 [`samples/bus-route-stops-1.json`](samples/bus-route-stops-1.json)에 저장했다.

## 활용 가능성

- 정류소 좌표 기반 출발지·목적지 주변 정류소 후보 계산
- `LINE_ID`와 `BUSSTOP_ID`를 이용한 노선 경유 관계 구성
- ARS ID를 시민 표시용 번호로 사용
