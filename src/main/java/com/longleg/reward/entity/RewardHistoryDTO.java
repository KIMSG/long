package com.longleg.reward.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RewardHistoryDTO {
    private final Long id;

    @JsonProperty("지급된 리워드")
    private final int points;

    @JsonProperty("지급 날짜")
    private final LocalDate requestDate;

    @JsonProperty("지급 여부")
    private final boolean totalPaid;

    @JsonProperty("지급 이유")
    private List<WorkActivityDTO> rewardReason;

    public RewardHistoryDTO(RewardHistory rewardHistory, LocalDate requestDate) {
        this.id = rewardHistory.getId();
        this.points = rewardHistory.getPoints();
        this.totalPaid = rewardHistory.isTotalPaid();
//        this.rewardReason = rewardHistory.getRewardReason();
        this.requestDate = requestDate; // ✅ 지급 요청 날짜 추가
    }
}