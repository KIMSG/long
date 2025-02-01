package com.longleg.service;

import com.longleg.exception.CustomException;
import com.longleg.repository.UserActivityRepository;
import com.longleg.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito 확장 적용
@DisplayName("WorkStatsService 단위 테스트")
class WorkStatsServiceTest {

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private WorkStatsService workStatsService;

    private Long workId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @BeforeEach
    void setUp() {
        workId = 100L;
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 2, 1);
        startDateTime = startDate.atStartOfDay();
        endDateTime = endDate.atTime(23, 59, 59);

        // ✅ Strict Stubbing 문제 해결 (필요할 경우 추가)
        lenient().when(workRepository.existsById(any(Long.class))).thenReturn(true);
        lenient().when(userActivityRepository.getWorkStats(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
    }


    @Test
    @DisplayName("getWorkStats - 작품이 존재하지 않으면 예외 발생")
    void getWorkStats_ThrowsException_WhenWorkNotFound() {
        // Given
        given(workRepository.existsById(workId)).willReturn(false);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                workStatsService.getWorkStats(workId, null, startDate, endDate));

        assertThat(exception.getMessage()).isEqualTo("해당 ID(" + workId + ")의 작품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("getWorkStats - 시작일이 종료일보다 클 경우 예외 발생")
    void getWorkStats_ThrowsException_WhenStartDateAfterEndDate() {
        // Given
        given(workRepository.existsById(workId)).willReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                workStatsService.getWorkStats(workId, null, endDate, startDate)); // 종료일이 시작일보다 앞섬

        assertThat(exception.getMessage()).isEqualTo("시작일(" + endDate + ")은 종료일(" + startDate + ")보다 클 수 없습니다.");
    }


    @Test
    @DisplayName("calculateDateRange - 기간별 날짜 범위 계산")
    void calculateDateRange_ReturnsCorrectRange() {
        // Given
        LocalDateTime now = LocalDateTime.now().withNano(0); // ✅ nano초 제거 (불필요한 차이 방지)

        // When
        Map<String, LocalDateTime> expectedRanges = Map.of(
                "daily", now.toLocalDate().atStartOfDay(),
                "weekly", now.minusWeeks(1),
                "monthly", now.minusMonths(1),
                "yearly", now.minusYears(1)
        );

        // ✅ null 전달하여 "daily" 기본값 테스트 추가
        LocalDateTime[] defaultRange = workStatsService.calculateDateRange(null, null, null);
        assertThat(defaultRange[0]).isEqualToIgnoringNanos(expectedRanges.get("daily"));
        assertThat(defaultRange[1]).isAfter(defaultRange[0]);

        // Then
        expectedRanges.forEach((period, expectedStart) -> {
            LocalDateTime[] range = workStatsService.calculateDateRange(period, null, null);
            assertThat(range[0]).isEqualToIgnoringNanos(expectedStart);
            assertThat(range[1]).isAfter(range[0]);
        });
    }

    @Test
    @DisplayName("formatStats - LIKE/UNLIKE 차이 계산 후 반환")
    void formatStats_CalculatesLikesAndReturnsFormattedMap() {
        // Given
        Map<String, Integer> rawStats = Map.of("LIKE", 20, "UNLIKE", 5, "VIEW", 300);

        // When
        Map<String, Integer> result = WorkStatsService.formatStats(rawStats);

        // Then
        assertThat(result.get("좋아요")).isEqualTo(15); // 20 - 5 = 15
        assertThat(result.get("조회수")).isEqualTo(300);
    }

    @Test
    @DisplayName("getWorkStats - userActivityRepository.getWorkStats()가 null일 경우 기본값 반환")
    void getWorkStats_ReturnsDefault_WhenStatsIsNull() {
        // Given
        given(workRepository.existsById(any(Long.class))).willReturn(true);
        given(userActivityRepository.getWorkStats(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(null); // ✅ any()를 사용하여 동적 날짜 값 처리

        // When
        Map<String, Integer> result = workStatsService.getWorkStats(workId, "monthly", startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("좋아요")).isEqualTo(0);
        assertThat(result.get("조회수")).isEqualTo(0);
    }

    @Test
    @DisplayName("getWorkStats - 통계 데이터가 비어 있을 경우 기본값 반환")
    void getWorkStats_ReturnsDefault_WhenStatsIsEmpty() {
        // Given
        given(workRepository.existsById(any(Long.class))).willReturn(true);
        given(userActivityRepository.getWorkStats(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(null); // ✅ any()를 사용하여 동적 날짜 값 처리

        // When
        Map<String, Integer> result = workStatsService.getWorkStats(workId, "weekly", startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("좋아요")).isEqualTo(0);
        assertThat(result.get("조회수")).isEqualTo(0);
    }

    @Test
    @DisplayName("getWorkStats - 데이터 변환이 올바르게 수행되는지 검증")
    void getWorkStats_TransformsDataCorrectly() {
        // Given
        given(workRepository.existsById(any(Long.class))).willReturn(true);
        given(userActivityRepository.getWorkStats(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(
                        new Object[]{"LIKE", 15},
                        new Object[]{"VIEW", 200}
                ));

        // When
        Map<String, Integer> result = workStatsService.getWorkStats(workId, "daily", startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("좋아요")).isEqualTo(15);
        assertThat(result.get("조회수")).isEqualTo(200);
    }


}
