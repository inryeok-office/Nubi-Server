# 광주 도시철도 공개데이터

## 공식 데이터

- 제공기관: 광주교통공사
- [역사정보 Open API](https://www.grtc.co.kr/subway/contents/stationInInfo)
  - JSON: `https://www.grtc.co.kr/subway/openapi/json/stationInformation?station_id={역ID}`
  - 주요 필드: `station_id`, `station_name`, `station_place`, 역사 면적 관련 필드
- [운행정보 Open API](https://www.grtc.co.kr/subway/contents/apiRunInfo)
  - JSON: `https://www.grtc.co.kr/subway/openapi/json/stationTimeInfomation?station_id={역ID}`
  - 주요 필드: `start_station_id`, `start_station_name`, `end_station_id`, `end_station_name`, `station_distance`(km), `station_time`(분)

## 인증 및 실제 호출

공식 문서에 예시 요청이 공개되어 있고, 2026-09-21 실제 JSON 호출에 성공했다. 별도 인증키는 확인되지 않았다.

샘플:

- [`samples/subway-station-1.json`](samples/subway-station-1.json)
- [`samples/subway-time-1.json`](samples/subway-time-1.json)

도시철도는 현재 저상버스 차량 식별 PoC의 핵심 경로가 아니므로, 연계는 후속 단계로 둔다.

