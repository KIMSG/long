package com.longleg.reward.entity;

public enum RewardStatus {
    REQUESTED,   // 리워드 지급 신청 (신청이 접수된 상태)
    PROGRESS, // 리워드 지급 진행 중
    COMPLETED,   // 리워드 지급 완료
    FAILED       // 리워드 지급 실패
}
