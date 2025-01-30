package com.longleg.reward.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkActivityDTO {
    private Long workId;
    private int likeCount;
    private int viewCount;

    // ✅ 점수 계산: (좋아요 * 2) + 조회수
    public int getScore() {
        return (likeCount * 2) + viewCount;
    }

    // 🛠 생성자 & getter 추가
    public WorkActivityDTO(Long workId, int likeCount, int viewCount) {
        this.workId = workId;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
    }

}


