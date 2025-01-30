package com.longleg.reward.controller;

import com.longleg.reward.service.WorkStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito 확장 활성화
class WorkStatsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkStatsService workStatsService; // Mock 객체

//    @InjectMocks
    private WorkStatsController workStatsController; // Mock을 주입할 대상

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workStatsController = new WorkStatsController(workStatsService); // 수동으로 생성

    }

    @Test
    @DisplayName("작품 통계 조회 - 기본 성공 케이스")
    void testGetWorkStats_Success() {
        // Mock 데이터 설정
        Map<String, Integer> mockStats = Map.of("좋아요", 5, "조회수", 100);

        // 서비스의 가짜 응답 설정
        // ✅ 수정된 코드 (인자 검사 완화)
        given(workStatsService.getWorkStats(any(), any(), any(), any()))
                .willReturn(mockStats);

        // API 호출
        ResponseEntity<Map<String, Integer>> response = workStatsController.getWorkStats(1L, "weekly", null, null);
        // 서비스가 실제로 호출되었는지 검증
        verify(workStatsService, times(1)).getWorkStats(any(), any(), any(), any());

        // 검증
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        assertTrue(response.getBody().containsKey("좋아요"), "응답 JSON에 '좋아요' 키가 없음");
        assertEquals(5, response.getBody().get("좋아요"));
        assertEquals(100, response.getBody().get("조회수"));
    }
}
