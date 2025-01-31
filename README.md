# 프로젝트 개요

## **목적**
이 프로젝트는 웹툰 플랫폼에서 소비자와 작가 간의 상호작용을 촉진하고, 작품의 랭킹 기반 리워드 시스템을 설계 및 구현하기 위해 만들어졌습니다.  
소비자와 작가의 활동 데이터를 활용하여 플랫폼의 경쟁력을 강화하고, 사용자 경험을 개선하는 것을 목표로 합니다.

### **🎯 기대 효과**
- **소비자:** 활동(조회, 좋아요)에 따라 리워드 획득 가능 → 플랫폼에서 지속적인 참여 유도.
- **작가:** 자신의 작품이 인기 작품으로 선정되면 보상을 받을 수 있음 → 창작 동기 부여.
- **운영자:** 사용자 데이터를 기반으로 인기 작품을 분석하고, 효과적인 프로모션 및 인센티브 정책을 기획할 수 있음.

### **📊 데이터 분석 흐름**
1. **사용자 활동 기록** → 조회 및 좋아요 데이터 저장.
2. **매일 랭킹 업데이트** → 조회수와 좋아요 가중치를 적용하여 순위 계산.
3. **리워드 지급 대상 선정** → 상위 10위 작품의 작가 및 기여 소비자에게 포인트 지급.
4. **리워드 내역 관리** → API 및 UI를 통해 사용자가 자신의 리워드 확인 가능.

## **주요 기능**
1. **데이터 수집**
    - 사용자의 **조회**, **좋아요**, **싫어요** 등의 활동을 저장.
    - `user_activity` 테이블에 사용자 활동 로그 기록.
    - `is_active` 상태를 기준으로 최신 상태 반영.

2. **작품 랭킹 계산**
    - **조회수 및 좋아요** 데이터를 기반으로 작품 랭킹 계산.
    - **가중치 적용 방식**: 조회수(1점) + 좋아요(3점).
    - 매일 자정마다 상위 10개 작품을 선정하여 랭킹 테이블 업데이트.

3. **리워드 지급 시스템**
    - 작가는 **자신의 작품이 랭킹에 포함**되었을 경우 포인트 지급.
    - 소비자는 **자신이 좋아요한 작품이 상위 랭킹에 포함**된 경우 보상 지급.

4. **리워드 내역 관리**
    - `/users/{id}/rewards` API를 통해 소비자/작가가 자신의 보상 내역 조회.
    - `/authors/{id}/rewards` API를 통해 작가 보상 조회 가능.

5. **H2 데이터베이스 연동**
    - 개발 및 테스트 환경에서는 **H2** 사용.
    - 실제 배포 환경에서는 MySQL 또는 PostgreSQL 사용 가능하도록 설정.

6. **도커 기반 배포**
    - `Dockerfile` 및 `docker-compose.yml`을 활용하여 애플리케이션 컨테이너화.
    - CI/CD 연동을 통해 자동 빌드 및 배포 지원.


## **기술 스택**
| 기술          | 버전        | 설명 |
|--------------|------------|----------------------------------|
| **Java**    | 17         | LTS 버전 |
| **Spring Boot** | 3.4.0  | 웹 애플리케이션 개발 |
| **Spring Data JPA** | 최신 | ORM을 활용한 데이터베이스 연동 |
| **H2 Database** | 최신 | 테스트 환경용 경량 데이터베이스 |
| **Swagger UI** | 최신 | API 문서 자동 생성 |
| **Lombok** | 최신 | 보일러플레이트 코드 제거 |
| **Gradle** | 최신 | 의존성 관리 및 빌드 도구 |
| **JaCoCo** | 최신 | 테스트 코드 커버리지 측정 |
| **Docker** | 최신 | 컨테이너 기반 배포 |


🚀 **추가 예정:**
- 도커 배포 고려
- MySQL/PostgreSQL 도입 예정



## 목표
- 랭킹 계산 및 리워드 지급을 효율적으로 처리할 수 있는 안정적이고 확장 가능한 시스템 설계.
- Docker 이미지를 통해 애플리케이션을 손쉽게 배포 및 실행 가능하도록 구현.
- API 중심 설계로 확장성과 유지보수성을 고려한 코드 작성.

---

## 1. 사용자 정의 예외 처리
- `CustomException`을 사용하여 예외를 정의하고, 명확한 에러 메시지와 코드로 처리했습니다.
- `GlobalExceptionHandler`를 통해 일관된 예외 응답을 제공하도록 설계했습니다.

---

## 2. RESTful API 설계
- HTTP 메서드에 따라 작업이 명확히 구분되었습니다.
    - **GET**: 데이터 조회.
    - **POST**: 데이터 생성.
    - **PUT**: 데이터 수정.
    - **DELETE**: 데이터 삭제.

## 3. 가독성을 고려한 변수 및 메서드 네이밍
- 클래스, 메서드, 변수 이름을 직관적으로 명명하여 코드의 의도를 명확히 했습니다.
    - 예: `validateMeetingRoom`, `validateParticipants`, `validateTimeConflict`.


---

## 프로젝트 구조

```
src
├── main
│   ├── java
│   │   └── com.longleg
│   │       ├── controller  # REST 컨트롤러
│   │       ├── dto         # 데이터 응용 모델
│   │       ├── entity      # 데이터 모델
│   │       ├── exception   # 공통 예외 처리 로직
│   │       ├── repository  # 데이터베이스 레이어
│   │       ├── service     # 비즈니스 로직
│   │       └── utils       # 공통유틸 모음
│   └── resources
│       ├── application.properties  # 애플리케이션 설정
│       ├── data.sql                # 초기 데이터 (선택사항)
│       └── static/TODO.md          # TODO 관리 파일
└── test
    └── java
        └── com.longleg  # 단위 테스트
```

---

# To-Do List

## 1. 프로젝트 테스트
### 1.1. 로컬 환경 테스트
- [ ] Docker 컨테이너 실행 확인:
    - `docker ps`를 사용하여 컨테이너 상태 확인.
- [ ] `./gradlew bootRun`으로 애플리케이션 실행 후 API 테스트.
- [ ] `http://localhost:8080/swagger-ui.html`에서 API 문서 확인.
- [ ] REST API 동작 확인:
    - `http://localhost:8080`에 접속하여 기본 API 테스트.
    - GET, POST 요청을 Postman 또는 cURL로 테스트.

### 1.2. Docker 이미지 검증
- [ ] Docker 이미지가 제대로 작동하는지 확인:
    - `docker build -t reward-app .`
    - `docker run -p 8080:8080 yuiox17/reward-app:1.0.0`

---

## 2. 문서화
### 2.1. README.md 작성
- [ ] 프로젝트 소개 작성:
    - 프로젝트 목적과 주요 기능 설명.
- [ ] Docker 실행 방법 작성:
    - Docker Hub에서 다운로드 및 실행 명령 포함.
    - Dockerfile을 사용해 빌드하는 방법 포함.
- [ ] REST API 명세서 작성:
    - 각 엔드포인트 설명, 요청/응답 예시 포함.

### 2.2. 기술 스택 및 아키텍처 문서
- [ ] 사용된 기술 스택 명시:
    - Java, Spring Boot, H2, Docker 등.
- [ ] 간단한 시스템 설계 설명:
    - 데이터 흐름, 주요 컴포넌트 간의 관계 요약.

---

## 3. 배포
### 3.1. Docker Hub 배포
- [ ] Docker Hub에 이미지 푸시:
    - `docker tag reward-app yuiox17/reward-app:1.0.0`
    - `docker push yuiox17/reward-app:1.0.0`
- [ ] Docker Hub 링크 확인:
    - 배포된 이미지를 다운로드 및 실행할 수 있는 URL 제공.

### 3.2. (선택 사항) 클라우드 배포
- [ ] AWS 또는 GCP에 배포 (선택 사항):
    - Docker 이미지를 사용해 컨테이너 실행.
    - 퍼블릭 URL 제공.

---

## 4. 코드 정리
### 4.1. 최종 코드 검토
- [ ] 불필요한 코드 및 디버그 로직 제거.
- [ ] 코드 스타일 검사 (Lombok 사용 확인, 적절한 주석 작성).

### 4.2. GitHub 업로드
- [ ] 프로젝트를 GitHub에 업로드:
    - `.gitignore` 파일에 `build/`, `*.jar` 등 제외.
- [ ] GitHub 저장소 URL 공유.

---

## 5. 추가 작업 (선택 사항)
- [ ] Unit Test 추가:
    - API 테스트를 위한 JUnit 테스트 코드 작성.
- [ ] 로깅 개선:
    - Logback 또는 SLF4J 설정 추가.
- [ ] 환경 분리:
    - `application.properties`를 프로파일(`dev`, `prod`)별로 분리.

---

## 테스트 및 코드

### 단위 테스트:
- src/test/java에서 테스트 코드 작성 및 실행.

### 코드 커버리지 리포트 (JaCoCo):
- 다음 명령어를 실행하여 테스트 및 커버리지 리포트를 생성합니다:
```
./gradlew clean test jacocoTestReport
```

- HTML 리포트 위치:
```
build/reports/jacoco/test/html/index.html
```

- 브라우저에서 index.html 파일을 열어 코드 커버리지 리포트를 확인합니다.

### 프로젝트 실행
   ```bash
   # 프로젝트 복제 및 실행
   git clone https://github.com/your-repo/our-room.git
   cd our-room
   ./gradlew bootRun
   ```

### Swagger 및 H2 Console 접속
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)


---


## todo list 관리하기

#### 마크다운으로 todo list를 관리
- **접속URL** :  /markdown
- **마크다운 파일 위치** : resources/static/TODO.md

---

## commit 관리하기

#### 타입(Type) 종류
- feat: 새로운 기능 추가
- fix: 버그 수정
- refactor: 코드 리팩토링 (기능 변화 없음)
- docs: 문서 수정 (README.md 등)
- test: 테스트 코드 추가/수정
- chore: 빌드 설정, 패키지 관리, 기타 작업
- style: 코드 스타일 수정 (포맷팅, 세미콜론 추가 등)
- perf: 성능 개선
- ci: CI/CD 설정 수정


---


## 최종 제출
1. Docker Hub 이미지 링크.
2. GitHub 저장소 링크 (코드 및 README 포함).
3. API 테스트 결과 (Postman 또는 테스트 스크립트).


---


## 참고 문서

- [Gradle 공식 문서](https://docs.gradle.org)
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.4.0/reference/using/devtools.html)
