package com.longleg.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkActivityDTO {
    private Long workId;
    private int likeCount;
    private int viewCount;
    private int rank;
    private Long userId;

    public WorkActivityDTO(Long workId) {
        this.workId = workId;
    }

    // ✅ 점수 계산: (좋아요 * 2) + 조회수
    public int getScore() {
        return (likeCount * 2) + viewCount;
    }

    // 🛠 생성자 & getter 추가
    public WorkActivityDTO(Long workId, int likeCount, int viewCount, Long userId) {
        this.workId = workId;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
    }

}


