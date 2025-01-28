package com.longleg.reward.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JacksonConfig
 * Jackson의 ObjectMapper를 커스터마이징하여 Spring 애플리케이션 전역에서 사용하도록 설정합니다.
 * 주로 Java 8 날짜 및 시간 API를 지원하고, 직렬화 형식을 조정합니다.
 */
@Configuration
public class JacksonConfig {

    /**
     * 커스터마이징된 ObjectMapper 빈 정의
     * @return JavaTimeModule이 등록되고 타임스탬프 직렬화가 비활성화된 ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 날짜 및 시간 API 모듈 등록
        mapper.registerModule(new JavaTimeModule());

        // 날짜와 시간을 ISO-8601 문자열 형식으로 직렬화 (타임스탬프 비활성화)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
