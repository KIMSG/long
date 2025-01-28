-- H2 Database 초기 데이터 스크립트
-- 테이블 삭제
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS works;
DROP TABLE IF EXISTS user_activities;
DROP TABLE IF EXISTS reward_requests;
DROP TABLE IF EXISTS reward_history;

-- 1. 사용자 테이블 생성 (작가 및 소비자 포함)
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       reward BIGINT,
                       role ENUM('AUTHOR', 'USER', 'BOTH') NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 초기 사용자 데이터
INSERT INTO users (name, reward, role) VALUES
                                           ('수사반장', 100, 'AUTHOR'),
                                           ('들개이빨', 0, 'AUTHOR'),
                                           ('김달', 0, 'AUTHOR'),
                                           ('박지', 0, 'AUTHOR'),
                                           ('엉덩국', 0, 'AUTHOR'),
                                           ('하우진', 0, 'AUTHOR'),
                                           ('허니트랩', 80, 'AUTHOR'),
                                           ('레바', 0, 'AUTHOR'),
                                           ('하리보', 0, 'AUTHOR'),
                                           ('사자', 100, 'USER'),
                                           ('호랑이', 50, 'USER'),
                                           ('독수리', 70, 'USER'),
                                           ('상어', 80, 'USER'),
                                           ('판다', 40, 'USER'),
                                           ('여우', 60, 'USER'),
                                           ('늑대', 90, 'USER'),
                                           ('용', 150, 'USER'),
                                           ('곰', 30, 'USER'),
                                           ('매', 60, 'USER'),
                                           ('강아지', 50, 'USER'),
                                           ('고양이', 40, 'USER'),
                                           ('토끼', 70, 'USER'),
                                           ('햄스터', 20, 'USER'),
                                           ('앵무새', 60, 'USER'),
                                           ('거북이', 30, 'USER'),
                                           ('고슴도치', 80, 'USER'),
                                           ('물고기', 20, 'USER'),
                                           ('말', 100, 'USER'),
                                           ('돌고래', 90, 'USER'),
                                           ('펭귄', 70, 'USER'),
                                           ('코알라', 30, 'USER'),
                                           ('기린', 50, 'USER'),
                                           ('수달', 40, 'USER'),
                                           ('코끼리', 120, 'USER');

-- 2. 작품 테이블 생성
CREATE TABLE works (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author_id BIGINT NOT NULL,
                       view_count INT DEFAULT 0,
                       like_count INT DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (author_id) REFERENCES users (id)
);

-- 초기 작품 데이터
INSERT INTO works (title, author_id) VALUES
                                         ('백억년을 자는 남자', 1),
                                         ('김철수씨 이야기', 1),
                                         ('먹는 존재', 2),
                                         ('달이 속삭이는 이야기', 3),
                                         ('레이디 셜록', 3),
                                         ('여자 제갈량', 3),
                                         ('남고 소년', 4),
                                         ('브리아노의 연구소', 5),
                                         ('하라는 공부는 안하고', 6),
                                         ('이웃집 길드원', 7),
                                         ('레바툰', 8),
                                         ('던전 속 사정[개정판]', 8),
                                         ('던전 속 사정', 8),
                                         ('딥 다운', 9),
                                         ('매드 독', 9),
                                         ('그 끝에 있는 것', 9);

-- 3. 활동 기록 테이블 생성 (조회 및 좋아요)
CREATE TABLE user_activities (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 work_id BIGINT NOT NULL,
                                 activity_type ENUM('VIEW', 'LIKE') NOT NULL,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (user_id) REFERENCES users (id),
                                 FOREIGN KEY (work_id) REFERENCES works (id)
);

-- 초기 활동 데이터 (조회와 좋아요)
INSERT INTO user_activities (user_id, work_id, activity_type) VALUES
                                                                  (2, 1, 'VIEW'),
                                                                  (2, 1, 'LIKE'),
                                                                  (3, 2, 'VIEW'),
                                                                  (3, 2, 'LIKE');

-- 4. 리워드 요청 테이블 생성
CREATE TABLE reward_requests (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 request_date DATE NOT NULL,
                                 status ENUM('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 초기 리워드 요청 데이터
INSERT INTO reward_requests (request_date, status) VALUES
                                                       ('2025-01-01', 'COMPLETED'),
                                                       ('2025-01-15', 'PENDING');

-- 5. 리워드 지급 내역 테이블 생성
CREATE TABLE reward_history (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                reward_request_id BIGINT NOT NULL,
                                receiver_id BIGINT NOT NULL,
                                receiver_type ENUM('AUTHOR', 'USER') NOT NULL,
                                work_id BIGINT NOT NULL,
                                points INT NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (reward_request_id) REFERENCES reward_requests (id),
                                FOREIGN KEY (receiver_id) REFERENCES users (id),
                                FOREIGN KEY (work_id) REFERENCES works (id)
);

-- 초기 리워드 내역 데이터
INSERT INTO reward_history (reward_request_id, receiver_id, receiver_type, work_id, points) VALUES
                                                                                                (1, 1, 'AUTHOR', 1, 100),
                                                                                                (1, 2, 'USER', 1, 50),
                                                                                                (1, 3, 'USER', 2, 30);
