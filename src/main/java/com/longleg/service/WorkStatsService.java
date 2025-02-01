package com.longleg.service;

import com.longleg.exception.CustomException;
import com.longleg.repository.UserActivityRepository;
import com.longleg.repository.WorkRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkStatsService {

    private final UserActivityRepository userActivityRepository;
    private final WorkRepository workRepository;  // 작품 존재 여부 확인을 위한 리포지토리

    /**
     * 기간(period) 또는 직접 지정한 날짜(startDate, endDate)를 기준으로 통계 조회
     */
    public Map<String, Integer> getWorkStats(Long workId, String period, LocalDate startDate, LocalDate endDate) {

        // ✅ null 값이 들어오면 안전한 기본값 설정
        LocalDate safeStartDate = Optional.ofNullable(startDate).orElse(LocalDate.MIN);
        LocalDate safeEndDate = Optional.ofNullable(endDate).orElse(LocalDate.MAX);

        // ✅ 1. 조회하려는 작품이 존재하는지 확인 (없으면 CustomException 발생)
        if (!workRepository.existsById(workId)) {
            throw new CustomException("Resource not found", "해당 ID(" + workId + ")의 작품을 찾을 수 없습니다.");
        }

        // ✅ 2. 시작일이 종료일보다 클 경우 CustomException 발생
        if (safeStartDate.isAfter(safeEndDate)) {
            throw new CustomException("Invalid to date", "시작일(" + safeStartDate + ")은 종료일(" + safeEndDate + ")보다 클 수 없습니다.");
        }
        // 기본값 설정
        period = Optional.ofNullable(period).orElse("daily");

        LocalDateTime[] dateRange = calculateDateRange(period, safeStartDate, safeEndDate);
        List<Object[]> stats = Optional.ofNullable(userActivityRepository.getWorkStats(workId, dateRange[0], dateRange[1]))
                .orElse(List.of()); // ✅ null 방지

        // 결과가 없을 경우 빈 Map 반환
        if (stats.isEmpty()) { // ✅ null 체크 제거
            return Map.of("좋아요", 0, "조회수", 0);
        }

        Map<String, Integer> result = new HashMap<>();
        for (Object[] stat : stats) {
            result.put((String) stat[0], ((Number) stat[1]).intValue());
        }

        return formatStats(result);

    }

    /**
     * 기간(period) 또는 사용자 입력(startDate, endDate)에 따라 날짜 범위 계산
     */
    LocalDateTime[] calculateDateRange(@Nullable String period,
                                       @Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        LocalDateTime endDateTime = LocalDateTime.now();
        LocalDateTime startDateTime;

        period = Optional.ofNullable(period).orElse("daily");
        switch (period) {
            case "weekly":
                startDateTime = endDateTime.minusWeeks(1);
                break;
            case "monthly":
                startDateTime = endDateTime.minusMonths(1);
                break;
            case "yearly":
                startDateTime = endDateTime.minusYears(1);
                break;
            default:
                startDateTime = endDateTime.toLocalDate().atStartOfDay(); // daily 기본값
        }
        return new LocalDateTime[]{startDateTime, endDateTime};
    }

    /**
     * LIKE - UNLIKE 값 계산 후 가독성 좋은 Map 반환
     */
    public static Map<String, Integer> formatStats(Map<String, Integer> stats) {
        System.out.println("📌 formatStats() 입력 데이터: " + stats);

        int likes = stats.getOrDefault("LIKE", 0) - stats.getOrDefault("UNLIKE", 0);
        int views = stats.getOrDefault("VIEW", 0);

        Map<String, Integer> result = new HashMap<>();
        result.put("좋아요", likes);
        result.put("조회수", views);
        System.out.println("✅ formatStats() 변환된 데이터: " + result);


        return result;
    }
}

