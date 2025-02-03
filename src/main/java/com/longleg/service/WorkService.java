package com.longleg.service;

import com.longleg.entity.ActivityType;
import com.longleg.entity.User;
import com.longleg.entity.UserActivity;
import com.longleg.entity.Work;
import com.longleg.exception.CustomException;
import com.longleg.repository.UserActivityRepository;
import com.longleg.repository.UserRepository;
import com.longleg.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;

    @Transactional
    public int recordView(Long workId, Long userId) {
        Work work = getWorkById(workId);
        User user = getUserById(userId);

        if (isRegularUser(user)) {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            boolean alreadyViewed = userActivityRepository.existsByUserAndWorkAndCreatedAtAfter(user, work, oneHourAgo);

            if (!alreadyViewed) {
                saveUserActivity(user, work, ActivityType.VIEW);
                work.increaseViewCount();
            }
        }
        return work.getViewCount();
    }

    @Transactional
    public int recordLike(Long workId, Long userId) {
        Work work = getWorkById(workId);
        User user = getUserById(userId);

        if (isRegularUser(user)) {
            if (userActivityRepository.isCurrentlyLiked(user, work)) {
                throw new CustomException("Resource already exists", "이미 좋아요를 한 작품입니다.");
            }
            saveUserActivity(user, work, ActivityType.LIKE);
            work.increaseLikeCount();
        }
        return work.getLikeCount();
    }

    @Transactional
    public void recordUnlike(Long workId, Long userId) {
        Work work = getWorkById(workId);
        User user = getUserById(userId);

        if (isRegularUser(user)) {
            if (!userActivityRepository.isCurrentlyLiked(user, work)) {
                throw new CustomException("Resource not found", "해당 작품을 좋아요하지 않아서 좋아요 취소를 할 수 없습니다.");
            }
            saveUserActivity(user, work, ActivityType.UNLIKE);
            work.decreaseLikeCount();
        }
    }



    // 🔹 공통 메서드 추가 🔹
    private Work getWorkById(Long workId) {
        return workRepository.findById(workId)
                .orElseThrow(() -> new CustomException("Resource not found", "해당 작품을 찾을 수 없습니다."));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Resource not found", "해당 사용자를 찾을 수 없습니다."));
    }

    private boolean isRegularUser(User user) {
        return "USER".equals(user.getUserRole().toString());
    }

    private void saveUserActivity(User user, Work work, ActivityType activityType) {
        userActivityRepository.save(UserActivity.builder()
                .user(user)
                .work(work)
                .activityType(activityType)
                .build());
    }
}

