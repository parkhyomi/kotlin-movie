# 🚀 영화 예매 (DB + HTTP API)

## 1. 기능 요구 사항

### 1.1 영화 조회 API

- `GET /api/movies` 요청 시 영화 목록과 상영 정보를 JSON으로 조회한다.
- 응답에는 영화 ID, 제목, 러닝타임, 상영 ID, 시작/종료 시각이 포함된다.

### 1.2 예매 생성 API

- `POST /api/reservations` 요청으로 여러 상영에 대해 좌석을 예매한다.
- 요청에는 `reservations`, `usedPoints`, `paymentMethod`가 포함된다.
- 응답에는 `reservationId`, 예매 좌석 목록, 결제 방식, `totalPrice`가 포함된다.

### 1.3 데이터베이스 영속성

- 영화/상영/예매 데이터는 H2 데이터베이스에 저장한다.
- 프로그램 재시작 후에도 파일 DB 기준으로 데이터가 유지된다.

### 1.4 좌석/상영 규칙

- 좌석은 `행+열` 형식(`A1`, `C3`)을 사용한다.
- 이미 예약된 좌석은 중복 예매할 수 없다.
- 한 번의 예매 요청 내에서도 중복 좌석은 허용하지 않는다.
- 겹치는 상영 시간은 함께 예매할 수 없다.

### 1.5 결제/할인 규칙

- 무비데이 할인(10일/20일/30일), 시간 할인(11시 이전/20시 이후), 포인트 차감, 결제수단 할인을 순서대로 적용한다.
- 총 결제 금액 계산은 기존 도메인(`PaymentCalculator`)을 재사용한다.

## 2. 프로그래밍 요구 사항

- 기존 `domain/model` 변경을 최소화한다.
- HTTP 계층이 추가되어도 핵심 비즈니스 로직의 책임은 도메인에 유지한다.
- HTTP 요청/응답 테스트와 DB 연동 테스트를 작성한다.
- 콘솔 흐름은 유지하고, API 흐름과 분리한다.

## 3. 프로젝트 구조

```text
src/main/kotlin
├── domain/model                  # 핵심 도메인
├── domain/backend
│   ├── controller                # HTTP Controller
│   ├── service                   # API 유스케이스
│   ├── dto                       # 요청/응답 DTO
│   ├── config                    # Bean/DB 초기화 설정
│   ├── repository                # Repository 포트 + 구현체
│   │   ├── jdbc
│   │   ├── inmemory
│   │   └── support
│   ├── facade                    # 콘솔 흐름 오케스트레이션
│   ├── factory                   # 콘솔용 객체 조립
│   └── parser                    # 좌석 코드 파싱
├── global
│   ├── exception                 # 전역 예외 처리
│   └── InitData                  # 초기 데이터 시드
└── view                          # 콘솔 입출력
```

## 4. 실행 방법

### 4.1 서버 실행

```bash
./gradlew bootRun
```

### 4.2 콘솔 실행

```bash
./gradlew run
```

## 5. 테스트 실행

### 5.1 전체 테스트

```bash
./gradlew test
```

### 5.2 API 통합 테스트

```bash
./gradlew test --tests "domain.backend.CinemaApiIntegrationTest"
```

### 5.3 빌드(ktlint + test 포함)

```bash
./gradlew clean build
```

## 6. 주요 응답 코드

- `200 OK`: 영화 목록 조회 성공
- `201 Created`: 예매 생성 성공
- `400 Bad Request`: 요청 형식 오류/유효성 오류
- `404 Not Found`: 존재하지 않는 상영
- `409 Conflict`: 중복 좌석/겹치는 상영

## 7. AI 도구 활용 기록

### 7.1 활용 방식

- 요구사항을 작은 작업 단위로 분해하고, 각 작업의 영향 범위(패키지/의존성/테스트)를 먼저 점검하는 용도로 AI를 사용했다.
- AI가 제안한 코드를 그대로 복사하지 않고, 기존 코드 구조와 도메인 규칙(`domain/model`)에 맞는지 확인한 뒤 수동으로 반영했다.
- 반영 후에는 `./gradlew build`로 실제 동작/테스트/스타일 규칙을 검증했다.
- 스프링 관련 코드 등 이해가 되지 않는 부분들에 대한 학습 및 코딩 보조도구로 많은 활용이 되었다 .

### 7.2 코드를 어떻게 수정했는지

- 기존 `api`, `controller`, `infra` 중심 코드를 `domain/backend` 기준 레이어로 재배치했다.
- 저장소 계층은 `domain/backend/repository` 아래 `jdbc`, `inmemory`, `support`로 분리했다.
- 예외 처리 클래스는 `global/exception`으로 이동하고, 서비스에서 `ServiceExceptionHandler`를 통해 예외를 생성하도록 통일했다.
