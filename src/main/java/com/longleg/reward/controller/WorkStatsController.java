package com.longleg.reward.controller;

import com.longleg.reward.service.WorkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
public class WorkStatsController {

    private final WorkStatsService workStatsService;

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Integer>> getWorkStats(
            @PathVariable Long id,
            @RequestParam(required = false) String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Integer> stats = workStatsService.getWorkStats(id, period, startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}

