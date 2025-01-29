package com.longleg.reward.controller;

import com.longleg.reward.exception.CustomException;
import com.longleg.reward.service.WorkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // ✅ JUnit 5 + Mockito 확장
class WorkControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkService workService;

//    @InjectMocks
    private WorkController workController;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // ✅ Mock 객체 초기화
        workController = new WorkController(workService); // ✅ 명시적으로 Mock 객체 주입
        mockMvc = MockMvcBuilders.standaloneSetup(workController)
                .setControllerAdvice(new Exception()) // ✅ 기본 예외 처리 적용
                .build(); // ✅ MockMvc 초기화
    }

    @Test
    @DisplayName("✅ 작품 조회 기록 저장 - 성공")
    void recordView_Success() throws Exception {
        // Given
        Mockito.when(workService.recordView(anyLong(), anyLong())).thenReturn(10);

        // When & Then
        mockMvc.perform(post("/works/1/view")
                        .param("userId", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    @DisplayName("✅ 작품 좋아요 추가 - 성공")
    void recordLike_Success() throws Exception {
        // Given
        Mockito.when(workService.recordLike(anyLong(), anyLong())).thenReturn(5); // ✅ 기대값 5

        // When & Then
        mockMvc.perform(post("/works/1/like")
                        .param("userId", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // ✅ 200 OK 기대
                .andExpect(content().string("5")); // ✅ 기대값과 일치
    }

    @Test
    @DisplayName("✅ 작품 좋아요 취소 - 성공")
    void unlikeWork_Success() throws Exception {
        // Given (Mockito는 void 메서드에 대해 예외가 발생하지 않으면 정상 실행됨)
        doNothing().when(workService).recordUnlike(anyLong(), anyLong());

        // When & Then
        mockMvc.perform(delete("/works/1/like")
                        .param("userId", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // ✅ 200 OK 기대
    }
}
