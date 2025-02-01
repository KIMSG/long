package com.longleg.controller;

import com.longleg.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class) // Mockito 확장 적용
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @DisplayName("사용자 리워드 내역 조회 - 성공")
    void getUserReward_Success() throws Exception {
        // Given
        Long userId = 1L;
        Map<String, Object> mockResponse = Map.of(
                "rewards", List.of(
                        Map.of("rewardAmount", 100, "date", "2024-02-01", "reason", "랭킹 보상"),
                        Map.of("rewardAmount", 50, "date", "2024-02-02", "reason", "활동 보상")
                ),
                "totalRewards", 2
        );

        given(userService.getUserReward(userId)).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rewards").isArray())
                .andExpect(jsonPath("$.totalRewards").value(2))
                .andExpect(jsonPath("$.rewards[0].rewardAmount").value(100))
                .andExpect(jsonPath("$.rewards[1].rewardAmount").value(50))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자 리워드 내역 조회 - 잘못된 ID 입력")
    void getUserReward_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/{id}", "abc")) // 숫자가 아닌 문자열 ID
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
