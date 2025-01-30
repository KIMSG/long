package com.longleg.reward.controller;

import com.longleg.reward.service.WorkStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
@Tag(name = "작품 통계 API", description = "작품의 조회수, 좋아요 수 등의 통계를 조회하는 API")
public class WorkStatsController {

    private final WorkStatsService workStatsService;

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "작품 통계 조회",
            description = "특정 작품의 조회수, 좋아요 수 등의 통계를 조회할 수 있습니다. "
                    + "`startDate`와 `endDate`를 지정하거나, `period`를 사용하여 미리 정의된 기간(daily, weekly, monthly, yearly)을 선택할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공적으로 작품 통계를 반환합니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 날짜 입력 (예: 시작일이 종료일보다 클 경우)")
    @ApiResponse(responseCode = "404", description = "작품을 찾을 수 없음")
    public ResponseEntity<Map<String, Integer>> getWorkStats(
            @PathVariable @Parameter(description = "조회할 작품 ID", example = "1") Long id,
            @RequestParam(required = false, defaultValue = "daily")
            @Parameter(description = "조회 기간 (daily, weekly, monthly, yearly 중 선택)", example = "weekly") String period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd 형식)", example = "2025-01-01") LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd 형식)", example = "2025-01-30") LocalDate endDate) {


        Map<String, Integer> stats = workStatsService.getWorkStats(id, period, startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}

