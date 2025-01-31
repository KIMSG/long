package com.longleg.reward.repository;

import com.longleg.reward.entity.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

//    // 특정 지급 요청 ID와 사용자 ID로 지급 여부 확인
//    boolean existsByReceiverIdAndRewardRequestIdAndTotalPaidTrue(Long receiverId, Long rewardRequestId);
//
//    // 특정 지급 요청 ID에 해당하는 지급 내역 조회 (아직 지급되지 않은 것만)
//    List<RewardHistory> findByRewardRequestIdAndTotalPaidFalse(Long rewardRequestId);

    @Modifying
    @Query(value = """
        UPDATE reward_history 
        SET total_paid = true
        WHERE receiver_id = :userId
        AND reward_request_id = :rewardRequestId
        AND total_paid = false
        """, nativeQuery = true)
    void markAsPaid(@Param("userId") Long userId, @Param("rewardRequestId") Long rewardRequestId);

    @Query(value = """
        SELECT rh.receiver_id, SUM(rh.points)
        FROM reward_history rh
        WHERE rh.total_paid = false
        AND rh.reward_request_id = :rewardRequestId
        GROUP BY rh.receiver_id
        """, nativeQuery = true)
    List<Object[]> findUnpaidRewardsByRequest(@Param("rewardRequestId") Long rewardRequestId);

}

