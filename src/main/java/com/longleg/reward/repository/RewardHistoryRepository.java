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

