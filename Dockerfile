# 1. Base Image 설정
FROM openjdk:17-jdk-slim

LABEL authors="gimseulgi"

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 4. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]