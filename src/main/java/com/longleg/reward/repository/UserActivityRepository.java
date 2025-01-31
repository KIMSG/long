package com.longleg.reward.repository;

import com.longleg.reward.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    boolean existsByUserAndWorkAndCreatedAtAfter(User user, Work work, LocalDateTime oneHourAgo);

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

    @Query(value = """
               SELECT 
                  ua.work_id AS workId,
                  COALESCE(SUM(CASE WHEN ua.activity_type = 'LIKE' THEN 1 ELSE 0 END), 0) 
                  - COALESCE(SUM(CASE WHEN ua.activity_type = 'UNLIKE' THEN 1 ELSE 0 END), 0) AS likeCount,
                  COALESCE(SUM(CASE WHEN ua.activity_type = 'VIEW' THEN 1 ELSE 0 END), 0) AS viewCount
               FROM user_activity ua 
               WHERE CAST(ua.created_at AS DATE) = :rewardDate
               GROUP BY ua.work_id
               """, nativeQuery = true)
    List<WorkActivityProjection> getWorkActivityCounts(LocalDate rewardDate);


    @Query(value = """
        SELECT DISTINCT user_id
        FROM user_activity
        WHERE work_id = :workId
          AND CAST(created_at AS DATE) = :rewardDate
        AND (
            activity_type = 'VIEW'
            OR (
                activity_type = 'LIKE'
                AND user_id NOT IN (
                    SELECT user_id
                    FROM user_activity
                    WHERE work_id = :workId
                      AND CAST(created_at AS DATE) = :rewardDate
                    AND activity_type = 'UNLIKE'
                    GROUP BY user_id
                    HAVING COUNT(*) >= (
                        SELECT COUNT(*)\s
                        FROM user_activity ua2
                        WHERE ua2.work_id = :workId
                          AND CAST(ua2.created_at AS DATE) = :rewardDate
                        AND ua2.activity_type = 'LIKE'
                        AND ua2.user_id = user_activity.user_id
                    )
                )
            )
        )
        """, nativeQuery = true)
    List<Long> findQualifiedUserIds(@Param("workId") Long workId, @Param("rewardDate") LocalDate rewardDate);

}
