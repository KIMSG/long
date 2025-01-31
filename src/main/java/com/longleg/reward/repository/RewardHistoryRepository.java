package com.longleg.reward.repository;

import com.longleg.reward.entity.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

    // 특정 지급 요청 ID와 사용자 ID로 지급 여부 확인
    boolean existsByReceiverIdAndRewardRequestIdAndTotalPaidTrue(Long receiverId, Long rewardRequestId);

    // 특정 지급 요청 ID에 해당하는 지급 내역 조회 (아직 지급되지 않은 것만)
    List<RewardHistory> findByRewardRequestIdAndTotalPaidFalse(Long rewardRequestId);
}

