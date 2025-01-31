package com.longleg.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "reward_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_request_id", nullable = false)
    private RewardRequest rewardRequest; // 지급 요청 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // 리워드를 받는 사용자 (작가 또는 소비자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = true)
    private Work work; // 리워드 지급 대상 작품

    private int points; // 지급된 포인트

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 지급된 날짜

    private boolean totalPaid = false; // 지급 여부 (기록만 남김 → 실제 지급 시 TRUE)

    private String rewardReason;

    public RewardHistory(RewardRequest rewardRequest, User receiver, Work work, int points) {
        this.rewardRequest = rewardRequest;
        this.receiver = receiver;
        this.work = work;
        this.points = points;
    }

    // ✅ work 없이 저장할 수 있는 생성자 추가
    public RewardHistory(RewardRequest rewardRequest, User receiver, int points) {
        this.rewardRequest = rewardRequest;
        this.receiver = receiver;
        this.work = null;  // NULL로 설정
        this.points = points;
    }

}
