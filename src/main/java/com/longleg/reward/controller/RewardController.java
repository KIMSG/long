package com.longleg.reward.controller;

import com.longleg.reward.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
@Tag(name = "리워드 관리", description = "랭킹 기반 리워드 지급 API")
public class RewardController {

    private final RewardService rewardService;

    @Operation(summary = "리워드 지급 요청", description = "특정 날짜의 리워드 지급을 요청합니다. (하루 한 번 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리워드 지급 요청 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (날짜 형식 오류 등)"),
            @ApiResponse(responseCode = "409", description = "해당 날짜에 이미 요청된 리워드 존재"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody  // ✅ JSON 변환을 위해 추가
    public ResponseEntity<Map<String, Object>> requestReward(@RequestParam("date") String date) {
        LocalDate rewardDate = LocalDate.parse(date);  // ✅ 날짜를 요청에서 받음
        Map<String, Object> response = rewardService.processReward(rewardDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "랭킹 정렬 조회", description = "특정 날짜 기준으로 상위 10개의 작품 랭킹을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping(value = "/sorted-works", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSortedWorks() {
        /*
        * 현재는 비즈니스 로직으로 처리하면서, 랭킹 계산에 대한 성능 문제가 발생하면 work_ranking 테이블을 추가하는 방향으로 확장
        * 작가에게는 리워드가 한번만 지급되어야 한다
        * A작가 작품이 1등 / 10등 이면 1등의 리워드 100, 10등의 리워드 10  이렇게 해서 합하여 110을 지급하기로함.
        * */

        LocalDate rewardDate = LocalDate.parse("2025-01-29");  // ✅ 날짜를 요청에서 받음
        Map<String, Object> response = rewardService.calReward(rewardDate);

        // ✅ JSON 응답 강제 설정
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping(value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getRewardExecute() {

        LocalDate rewardDate = LocalDate.parse("2025-01-29");
        Map<String, Object> response = rewardService.rewardExecute(rewardDate);

        // ✅ JSON 응답 강제 설정
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
