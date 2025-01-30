package com.longleg.reward.repository;

import com.longleg.reward.entity.*;
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

//    @Query("SELECT new com.longleg.reward.entity.WorkActivityDTO(" +
//            "    ua.work.id, " +
//            "    COALESCE(SUM(CASE WHEN ua.activityType = 'LIKE' THEN 1 ELSE 0 END), 0) " +
//            "    - COALESCE(SUM(CASE WHEN ua.activityType = 'UNLIKE' THEN 1 ELSE 0 END), 0), " +
//            "    COALESCE(SUM(CASE WHEN ua.activityType = 'VIEW' THEN 1 ELSE 0 END), 0)) " +
//            "FROM UserActivity ua " +
//            "GROUP BY ua.work.id")

    @Query(value = """
               SELECT 
                  ua.work_id AS workId,
                  COALESCE(SUM(CASE WHEN ua.activity_type = 'LIKE' THEN 1 ELSE 0 END), 0) 
                  - COALESCE(SUM(CASE WHEN ua.activity_type = 'UNLIKE' THEN 1 ELSE 0 END), 0) AS likeCount,
                  COALESCE(SUM(CASE WHEN ua.activity_type = 'VIEW' THEN 1 ELSE 0 END), 0) AS viewCount
               FROM user_activity ua 
               GROUP BY ua.work_id
               """, nativeQuery = true)
    List<WorkActivityProjection> getWorkActivityCounts();



}
