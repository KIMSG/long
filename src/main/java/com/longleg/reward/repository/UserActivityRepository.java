package com.longleg.reward.repository;

import com.longleg.reward.entity.UserActivity;
import com.longleg.reward.entity.User;
import com.longleg.reward.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    boolean existsByUserAndWorkAndCreatedAtAfter(User user, Work work, LocalDateTime time);
}
