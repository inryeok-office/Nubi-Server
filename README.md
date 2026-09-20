# NUBI Server

광주광역시 교통약자 이동지원 및 이동권 분석 플랫폼 NUBI의 서버 저장소입니다.

현재 저장소는 기관 문의 전 기술 PoC 단계입니다. 초기 검증의 우선순위는 광주 버스 실시간 데이터에서 특정 운행 차량의 저상버스 여부를 공개데이터만으로 식별할 수 있는지 확인하는 것입니다.

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

헬스 체크: `http://localhost:8080/actuator/health`

## 검증

```bash
./gradlew test
./gradlew ktlintCheck
./gradlew bootJar
```

`test`는 Testcontainers의 PostGIS 컨테이너를 사용하므로 Docker가 실행 중이어야 합니다.

공공 API 인증키는 커밋하지 않으며 환경변수로만 주입합니다. 데이터 조사 결과는 [`docs/data-research/`](docs/data-research/)에 기록합니다.
