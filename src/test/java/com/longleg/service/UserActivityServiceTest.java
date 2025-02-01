package com.longleg.service;


import com.longleg.repository.UserActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito 확장 적용
@DisplayName("UserActivityService 단위 테스트")
class UserActivityServiceTest {

    @Mock
    private UserActivityRepository userActivityRepository;

    @InjectMocks
    private UserActivityService userActivityService;

    private Long workId;
    private LocalDate rewardDate;

    @BeforeEach
    void setUp() {
        workId = 100L;
        rewardDate = LocalDate.of(2024, 2, 1);
    }

    @Test
    @DisplayName("getQualifiedUsers - 정상적인 사용자 ID 목록 반환")
    void getQualifiedUsers_ReturnsUserIds() {
        // Given
        List<Long> mockUserIds = List.of(1L, 2L, 3L);
        given(userActivityRepository.findQualifiedUserIds(workId, rewardDate))
                .willReturn(mockUserIds);

        // When
        List<Long> result = userActivityService.getQualifiedUsers(workId, rewardDate);

        // Then
        assertThat(result).isNotNull()
                .hasSize(3)
                .containsExactly(1L, 2L, 3L);

        verify(userActivityRepository).findQualifiedUserIds(workId, rewardDate);
    }

    @Test
    @DisplayName("getQualifiedUsers - 조회된 사용자 ID가 없을 경우 빈 리스트 반환")
    void getQualifiedUsers_ReturnsEmptyList() {
        // Given
        given(userActivityRepository.findQualifiedUserIds(workId, rewardDate))
                .willReturn(List.of());

        // When
        List<Long> result = userActivityService.getQualifiedUsers(workId, rewardDate);

        // Then
        assertThat(result).isEmpty();

        verify(userActivityRepository).findQualifiedUserIds(workId, rewardDate);
    }
}