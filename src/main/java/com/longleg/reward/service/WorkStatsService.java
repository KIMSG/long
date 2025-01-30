package com.longleg.reward.service;

import com.longleg.reward.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkStatsService {

    private final UserActivityRepository userActivityRepository;

    public Map<String, Integer> getWorkStatsPeriod(Long workId, String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period) {
            case "weekly":
                startDate = endDate.minusWeeks(1);
                break;
            case "monthly":
                startDate = endDate.minusMonths(1);
                break;
            case "yearly":
                startDate = endDate.minusYears(1);
                break;
            default:
                startDate = endDate.toLocalDate().atStartOfDay(); // 하루 기준
        }

        List<Object[]> stats = userActivityRepository.getWorkStats(workId, startDate, endDate);
        Map<String, Integer> result = new HashMap<>();

        for (Object[] stat : stats) {
            result.put((String) stat[0], ((Number) stat[1]).intValue());
        }

        return result;
    }

    public Map<String, Integer> getWorkStats(Long workId, String period, LocalDate startDate, LocalDate endDate) {
//        LocalDateTime startDateTime = startDate.atStartOfDay(); // 시작 날짜 00:00:00
//        LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 종료 날짜 23:59:59
//
//        List<Object[]> stats = userActivityRepository.getWorkStats(workId, startDateTime, endDateTime);
//        Map<String, Integer> result = new HashMap<>();
//
//        for (Object[] stat : stats) {
//            result.put((String) stat[0], ((Number) stat[1]).intValue());
//        }
//
//        return result;

        LocalDateTime startDateTime;
        LocalDateTime endDateTime = LocalDateTime.now();

        if (startDate != null && endDate != null) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(23, 59, 59);
        } else {
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
        }

        List<Object[]> stats = userActivityRepository.getWorkStats(workId, startDateTime, endDateTime);
        Map<String, Integer> result = new HashMap<>();

        for (Object[] stat : stats) {
            result.put((String) stat[0], ((Number) stat[1]).intValue());
        }

        return result;
    }
}

