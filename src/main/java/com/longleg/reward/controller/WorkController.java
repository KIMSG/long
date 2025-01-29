package com.longleg.reward.controller;

import com.longleg.reward.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @PostMapping("/{id}/view")
    public ResponseEntity<Integer> recordView(
            @PathVariable Long id,
            @RequestParam Long userId ) {

        int updatedViewCount = workService.recordView(id, userId);
        return ResponseEntity.ok(updatedViewCount);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Integer> recordLike(
            @PathVariable Long id,
            @RequestParam Long userId ) {

        int updatedLikeCount = workService.recordLike(id, userId);
        return ResponseEntity.ok(updatedLikeCount);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Integer> unlikeWork(@PathVariable Long id, @RequestParam Long userId) {
        int likeCount = workService.recordUnlike(id, userId);
        return ResponseEntity.ok(likeCount);
    }
}
