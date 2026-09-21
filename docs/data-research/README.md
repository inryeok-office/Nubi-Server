# 광주 교통 공공데이터 조사

이 문서는 기관 문의 전 기술 PoC에서 확인한 광주 교통 공개데이터의 출처와 관찰 결과를 정리한다.

조사 기준일: 2026-09-21 (Asia/Seoul)

## 공식 출처

- [전남광주통합특별시_광주버스정보](https://www.data.go.kr/data/15157923/openapi.do)
  - 제공기관: 전남광주통합특별시, 관리부서: 대중교통과
  - 노선-정류소, 노선, 정류소, 도착, 노선별 버스위치 정보를 하나의 OpenAPI 설명으로 제공
  - REST, JSON+XML, 무료, 이용허락범위 제한 없음
  - 개발계정 신청 가능 트래픽은 일 100회이며 인증키가 필요함
- [광주광역시교통정보센터 Open API 안내](https://www.gjtic.go.kr/open-api)
  - 광주 교통정보센터는 Open API를 공공데이터포털을 통해 신청·사용하도록 안내
- [광주광역시 버스정보 공식 사이트](https://bus.gwangju.go.kr/main/main)
  - 웹 화면에서 노선, 정류소, 도착 및 실시간 위치를 제공
  - 아래 문서의 `*Temp2` 경로는 이 공식 웹 화면이 호출하는 실제 공개 응답을 관찰한 것이며, 별도 안정 API 계약으로 간주하지 않는다.
- [광주교통공사 도시철도 운행정보 Open API](https://www.grtc.co.kr/subway/contents/apiRunInfo)
  - JSON: `https://www.grtc.co.kr/subway/openapi/json/stationTimeInfomation`
  - XML: `https://www.grtc.co.kr/subway/openapi/xml/stationTimeInfomation`

## 조사 방법과 제한

- 공공데이터포털 공식 문서와 공식 운영 사이트만 1차 근거로 사용했다.
- 광주 버스 OpenAPI는 인증키가 필요하므로 키를 만들거나 입력하지 않았다. 따라서 공공데이터포털 API의 인증된 호출 성공 여부는 미확인이다.
- 인증 없이 접근 가능한 공식 버스정보 사이트의 실제 JSON 응답과 광주교통공사 도시철도 API의 실제 JSON 응답만 샘플로 보관했다.
- 샘플은 API Key, 개인정보 및 세션 값이 없는 응답만 포함한다.

## 문서 목록

- [노선](bus-route.md)
- [정류소 및 노선-정류소](bus-stop.md)
- [도착정보](bus-arrival.md)
- [실시간 위치정보](bus-position.md)
- [저상버스 식별](low-floor-bus.md)
- [도시철도](subway.md)
- [전체 타당성 보고서](feasibility-report.md)

