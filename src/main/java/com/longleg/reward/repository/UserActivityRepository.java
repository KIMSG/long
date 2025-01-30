package com.longleg.reward.repository;

import com.longleg.reward.entity.ActivityType;
import com.longleg.reward.entity.UserActivity;
import com.longleg.reward.entity.User;
import com.longleg.reward.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    boolean existsByUserAndWorkAndCreatedAtAfter(User user, Work work, LocalDateTime oneHourAgo);
    boolean existsByUserAndWorkAndActivityType(User user, Work work, ActivityType activityType);  // ✅ 중복 좋아요 확인

    @Transactional
    void deleteByUserAndWorkAndActivityType(User user, Work work, ActivityType activityType);

    // ✅ 현재 유저가 "좋아요"를 유지하고 있는지 확인 (취소된 'UNLIKE'가 없고, 최신 'LIKE'가 있는 경우)
    @Query("SELECT COUNT(ua) > 0 FROM UserActivity ua " +
            "WHERE ua.user = :user AND ua.work = :work " +
            "AND ua.activityType = 'LIKE' " +
            "AND NOT EXISTS (SELECT 1 FROM UserActivity u2 " +
            "WHERE u2.user = ua.user AND u2.work = ua.work AND u2.activityType = 'UNLIKE' AND u2.createdAt > ua.createdAt)")
    boolean isCurrentlyLiked(User user, Work work);


    @Query("SELECT CAST(ua.activityType AS string), COUNT(ua) " +
            "FROM UserActivity ua " +
            "WHERE ua.work.id = :workId " +
            "AND ua.createdAt BETWEEN :startDate AND :endDate " +
            "AND ua.isActive = TRUE " +
            "GROUP BY ua.activityType")
    List<Object[]> getWorkStats(@Param("workId") Long workId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
}
