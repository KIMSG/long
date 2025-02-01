package com.longleg.controller;

import com.longleg.entity.RewardHistory;
import com.longleg.service.UserService;
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

import java.util.Map;

@RestController
@RequestMapping(value = "/users") // JSON UTF-8 설정
@RequiredArgsConstructor
@Tag(name = "리워드 내역 관리", description = "리워드 내역 확인")
public class UserController {

    private final UserService userService;

    /**
     * 특정 사용자의 리워드 내역을 조회하는 API
     *
     * @param id 조회할 사용자 ID
     * @return 사용자의 리워드 내역과 총 개수를 포함한 응답
     */
    @Operation(
            summary = "사용자 리워드 내역 조회",
            description = """
            특정 사용자의 리워드 내역과 지급 사유를 조회하는 API입니다.
            - 지급된 리워드 개수
            - 지급된 날짜
            - 지급된 이유 (예: 랭킹 순위 보상, 활동 내역 등)
            - 총 지급 내역 개수

            이 API는 사용자 ID를 입력받아 해당 사용자의 리워드 정보를 반환합니다.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 리워드 내역을 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RewardHistory.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (예: ID 값이 유효하지 않음)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        Map<String, Object> stats = userService.getUserReward(id);
        return ResponseEntity.ok(stats);
    }
}
