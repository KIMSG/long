package com.longleg.controller;

import com.longleg.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RewardControllerTest {

    @InjectMocks
    private RewardController rewardController;

    @Mock
    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    @DisplayName("리워드 지급 요청 API - 성공")
//    void requestReward_success() {
//        // Given
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "리워드 지급 요청이 완료되었습니다.");
//        response.put("requestDate", "2025-01-29");
//        response.put("status", "COMPLETED");
//
//        when(rewardService.processReward(any(LocalDate.class))).thenReturn(response);
//
//        // When
//        ResponseEntity<Map<String, Object>> result = rewardController.requestReward();
//
//        // Then
//        assertEquals(200, result.getStatusCodeValue());
//        assertEquals(response, result.getBody());
//    }

    @Test
    @DisplayName("랭킹 정렬 조회 API - 성공")
    void getSortedWorks_success() {
        // Given
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reward request completed.");
        response.put("requestDate", "2025-01-29");
        response.put("status", "COMPLETED");

        when(rewardService.calReward(any(LocalDate.class))).thenReturn(response);

        // When
        ResponseEntity<Map<String, Object>> result = rewardController.getSortedWorks("2025-01-29");

        // Then
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(response, result.getBody());
    }
}
