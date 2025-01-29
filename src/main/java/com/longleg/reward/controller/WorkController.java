package com.longleg.reward.controller;

import com.longleg.reward.service.WorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @Operation(
            summary = "작품 조회 기록 저장",
            description = """
        특정 작품을 조회할 때, 해당 조회 기록을 저장하는 API입니다.  
        - 조회하는 사용자가 `일반 유저(USER)`일 경우에만 조회수가 증가합니다.  
        - 같은 사용자가 1시간 이내에 같은 작품을 다시 조회하면 중복 기록되지 않습니다.  
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 기록 저장 성공",
                    content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "404", description = "작품 또는 사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "{\"errorCode\": \"Resource not found\", \"errorMessage\": \"해당 작품을 찾을 수 없습니다.\"}")))
    })
    @PostMapping("/{id}/view")
    public ResponseEntity<Integer> recordView(
            @PathVariable Long id,
            @RequestParam Long userId ) {

        int updatedViewCount = workService.recordView(id, userId);
        return ResponseEntity.ok(updatedViewCount);
    }

    @Operation(
            summary = "작품 좋아요 추가",
            description = """
        특정 작품에 좋아요를 추가하는 API입니다.  
        - `좋아요(LIKE)` 기록은 삭제되지 않고 유지됩니다.  
        - 같은 사용자가 이미 좋아요를 한 경우, 중복 요청을 방지합니다.  
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 추가 성공",
                    content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "409", description = "이미 좋아요한 작품",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "{\"errorCode\": \"Resource already exists\", \"errorMessage\": \"이미 좋아요를 한 작품 입니다.\"}")))
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<Integer> recordLike(
            @PathVariable Long id,
            @RequestParam Long userId ) {

        int updatedLikeCount = workService.recordLike(id, userId);
        return ResponseEntity.ok(updatedLikeCount);
    }

    @Operation(
            summary = "작품 좋아요 취소",
            description = """
        특정 작품의 좋아요를 취소하는 API입니다.  
        - 좋아요 취소(`UNLIKE`)도 기록으로 남으며, 기존 `LIKE` 기록은 삭제되지 않습니다.  
        - 좋아요를 하지 않은 작품에 대해 취소 요청을 하면 예외가 발생합니다.  
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공",
                    content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "404", description = "좋아요하지 않은 작품",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "{\"errorCode\": \"Resource not found\", \"errorMessage\": \"해당 작품을 좋아요하지 않아서 좋아요 취소를 할 수 없습니다.\"}")))
    })
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Integer> unlikeWork(@PathVariable Long id, @RequestParam Long userId) {
        int likeCount = workService.recordUnlike(id, userId);
        return ResponseEntity.ok(likeCount);
    }
}
