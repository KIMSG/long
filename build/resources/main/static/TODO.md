# 📋 프로젝트 To-Do List

## 1. 엔티티 설계  (12.21 토)
- [X] Users 엔티티 생성 (`feature/entity-setup`)
    - 사용자 ID, 이름 필드 정의
    - MeetingRoom과의 Many-to-One 관계 매핑
- [X] MeetingRoom 엔티티 생성 (`feature/entity-setup`)
    - 회의실 ID, 이름, 수용 인원 필드 정의
    - Users와의 One-to-Many 관계 매핑
- [X] Schedule 엔티티 생성 (`feature/entity-setup`)
    - 일정 ID, 이름, 시작/종료 시간, 참여자 목록 필드 정의
    - MeetingRoom과의 Many-to-One 관계 매핑
    - Users와의 Many-to-Many 관계 매핑

---

## 2. 사용자 관리 API (12.21 토)
- [X] 사용자 생성 (`feature/user-api`)
    - POST /users
- [X] 사용자 조회 (`feature/user-api`)
    - GET /users
    - GET /users/{id}

---

## 3. 회의실 관리 API (12.21 토)
- [X] 회의실 조회 (`feature/meetingroom-api`)
    - GET /rooms
    - GET /rooms/{id}
- [X] 회의실 예약 가능 여부 확인 (`feature/meetingroom-api`)
    - GET /rooms/{id}/availability

---

## 4. 일정 관리 API (12.22 일)
- [X] 일정 생성 (`feature/schedule-api`)
    - POST /schedules
- [X] 일정 조회 (`feature/schedule-api`)
    - GET /schedules
    - GET /schedules/{id}
- [X] 일정 수정 (`feature/schedule-api`)
    - PUT /schedules/{id}
- [X] 일정 삭제 (`feature/schedule-api`)
    - DELETE /schedules/{id}
- [X] 일정에 참여자 추가 (`feature/schedule-participants`)
    - POST /schedules/{id}/participants
- [X] 일정에서 참여자 삭제 (`feature/schedule-participants`)
    - DELETE /schedules/{id}/participants/{userId}

---


## 6. 테스트 및 디버깅 (12.23 월)
- [X] 엔티티와 초기 데이터 테스트 (`feature/test-database`)
    - Jacoco과 H2 콘솔을 활용하여 데이터 무결성 확인
- [X] API 통합 테스트 (`feature/test-api`)
    - MockMvc로 API 호출 및 응답 테스트
    - 상태 코드 및 응답 데이터 검증

---

## 7. 문서화 (기능 개발 마다)
- [X] API 명세 작성 (`feature/docs-api`)
    - 각 API에 대한 요청/응답 정의
- [X] 프로젝트 README 작성 (`docs/readme`)
    - 프로젝트 개요 및 실행 방법
    - 주요 기능 및 구조 설명

---

## 8. 최종 마무리 (12.24 화)
- [X] 코드 리팩토링 (`chore/refactor-code`)
    - 코드 정리 및 주석 추가
    - 불필요한 코드 및 로그 제거
- [X] 최종 배포 준비 (`main`)
    - 배포 가능한 상태로 병합


