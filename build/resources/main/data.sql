-- H2 Database 초기 데이터 스크립트

DROP TABLE IF EXISTS reward_requests CASCADE;
DROP TABLE IF EXISTS reward_history CASCADE;
DROP TABLE IF EXISTS user_activities CASCADE;
DROP TABLE IF EXISTS user_activity CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS work CASCADE;
DROP TABLE IF EXISTS works CASCADE;

-- 1. 사용자 테이블 생성 (작가 및 소비자 포함)
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       reward BIGINT,
                       user_role VARCHAR(20) NOT NULL,  -- 🚀 ENUM 대신 VARCHAR 사용
                       created_at TIMESTAMP NOT NULL
);

-- 초기 사용자 데이터
INSERT INTO users (name, reward, user_role, created_at) VALUES
                                           ('수사반장', 100, 'AUTHOR', NOW()),
                                           ('들개이빨', 0, 'AUTHOR', NOW()),
                                           ('김달', 0, 'AUTHOR', NOW()),
                                           ('박지', 0, 'AUTHOR', NOW()),
                                           ('엉덩국', 0, 'AUTHOR', NOW()),
                                           ('하우진', 0, 'AUTHOR', NOW()),
                                           ('허니트랩', 80, 'AUTHOR', NOW()),
                                           ('레바', 0, 'AUTHOR', NOW()),
                                           ('하리보', 0, 'AUTHOR', NOW()),
                                           ('사자', 100, 'USER', NOW()),
                                           ('호랑이', 50, 'USER', NOW()),
                                           ('독수리', 70, 'USER', NOW()),
                                           ('상어', 80, 'USER', NOW()),
                                           ('판다', 40, 'USER', NOW()),
                                           ('여우', 60, 'USER', NOW()),
                                           ('늑대', 90, 'USER', NOW()),
                                           ('용', 150, 'USER', NOW()),
                                           ('곰', 30, 'USER', NOW()),
                                           ('매', 60, 'USER', NOW()),
                                           ('강아지', 50, 'USER', NOW()),
                                           ('고양이', 40, 'USER', NOW()),
                                           ('토끼', 70, 'USER', NOW()),
                                           ('햄스터', 20, 'USER', NOW()),
                                           ('앵무새', 60, 'USER', NOW()),
                                           ('거북이', 30, 'USER', NOW()),
                                           ('고슴도치', 80, 'USER', NOW()),
                                           ('물고기', 20, 'USER', NOW()),
                                           ('말', 100, 'USER', NOW()),
                                           ('돌고래', 90, 'USER', NOW()),
                                           ('펭귄', 70, 'USER', NOW()),
                                           ('코알라', 30, 'USER', NOW()),
                                           ('기린', 50, 'USER', NOW()),
                                           ('수달', 40, 'USER', NOW()),
                                           ('코끼리', 120, 'USER', NOW());

-- 2. 작품 테이블 생성
CREATE TABLE works (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author_id BIGINT NOT NULL,
                       view_count INT DEFAULT 0,
                       like_count INT DEFAULT 0,
                       created_at TIMESTAMP NOT NULL,
                       FOREIGN KEY (author_id) REFERENCES users (id)
);

-- 초기 작품 데이터
INSERT INTO works (title, author_id, created_at) VALUES
                                         ('백억년을 자는 남자', 1, NOW()),
                                         ('김철수씨 이야기', 1, NOW()),
                                         ('먹는 존재', 2, NOW()),
                                         ('달이 속삭이는 이야기', 3, NOW()),
                                         ('레이디 셜록', 3, NOW()),
                                         ('여자 제갈량', 3, NOW()),
                                         ('남고 소년', 4, NOW()),
                                         ('브리아노의 연구소', 5, NOW()),
                                         ('하라는 공부는 안하고', 6, NOW()),
                                         ('이웃집 길드원', 7, NOW()),
                                         ('레바툰', 8, NOW()),
                                         ('던전 속 사정[개정판]', 8, NOW()),
                                         ('던전 속 사정', 8, NOW()),
                                         ('딥 다운', 9, NOW()),
                                         ('매드 독', 9, NOW()),
                                         ('그 끝에 있는 것', 9, NOW());

-- 3. 활동 기록 테이블 생성 (조회 및 좋아요)
CREATE TABLE user_activity (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 work_id BIGINT NOT NULL,
                                 activity_type VARCHAR(10) NOT NULL DEFAULT 'VIEW',
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at TIMESTAMP NOT NULL,
                                 FOREIGN KEY (user_id) REFERENCES users (id),
                                 FOREIGN KEY (work_id) REFERENCES works (id)
);

-- 초기 활동 데이터 (조회와 좋아요)
INSERT INTO user_activity (user_id, work_id, activity_type, created_at) VALUES
                                                                  (2, 1, 'VIEW', NOW()),
                                                                  (2, 1, 'LIKE', NOW()),
                                                                  (3, 2, 'VIEW', NOW()),
                                                                  (3, 2, 'LIKE', NOW());

-- 4. 리워드 요청 테이블 생성
CREATE TABLE reward_requests (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 request_date DATE NOT NULL,
                                 status varchar2(20) DEFAULT 'PENDING',
                                 created_at TIMESTAMP NOT NULL
);

-- 초기 리워드 요청 데이터
INSERT INTO reward_requests (request_date, status, created_at) VALUES
                                                       ('2025-01-01', 'COMPLETED', NOW()),
                                                       ('2025-01-15', 'PENDING', NOW());

-- 5. 리워드 지급 내역 테이블 생성
CREATE TABLE reward_history (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                reward_request_id BIGINT NOT NULL,
                                receiver_id BIGINT NOT NULL,
                                receiver_type varchar2(20) NOT NULL,
                                work_id BIGINT NOT NULL,
                                points INT NOT NULL,
                                created_at TIMESTAMP NOT NULL,
                                FOREIGN KEY (reward_request_id) REFERENCES reward_requests (id),
                                FOREIGN KEY (receiver_id) REFERENCES users (id),
                                FOREIGN KEY (work_id) REFERENCES works (id)
);

-- 초기 리워드 내역 데이터
INSERT INTO reward_history (reward_request_id, receiver_id, receiver_type, work_id, points, created_at) VALUES
                                                                                                (1, 1, 'AUTHOR', 1, 100, NOW()),
                                                                                                (1, 2, 'USER', 1, 50, NOW()),
                                                                                                (1, 3, 'USER', 2, 30, NOW());
