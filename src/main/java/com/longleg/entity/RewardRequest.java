package com.longleg.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_requests")
@Getter
@Setter
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
        this.status = RewardStatus.REQUESTED;
        this.createdAt = LocalDateTime.now();
    }

    public RewardRequest(long l) {
    }

    /** ✅ 리워드 지급을 시작할 때 호출 */
    public void startProcessing() {
        this.status = RewardStatus.PROGRESS;
    }

    /** ✅ 리워드 지급이 성공적으로 완료되었을 때 호출 */
    public void complete() {
        this.status = RewardStatus.COMPLETED;
    }

    /** ✅ 리워드 지급이 실패했을 때 호출 */
    public void fail() {
        this.status = RewardStatus.FAILED;
    }
}

