package com.longleg.reward.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_date", columnDefinition = "DATE", nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    private RewardStatus status;

    private LocalDateTime createdAt;

    public RewardRequest(LocalDate requestDate) {
        this.requestDate = requestDate;
        this.status = RewardStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = RewardStatus.COMPLETED;
    }
}

