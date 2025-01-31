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
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException("Resource not found","해당 작품을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Resource not found","해당 사용자를 찾을 수 없습니다."));

        //조회하는 사람이 일반 유저일 때만~ 조회수 올리기
        if (user.getUserRole().toString().equals("USER")) {

            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

            boolean alreadyViewed = userActivityRepository.existsByUserAndWorkAndCreatedAtAfter(user, work, oneHourAgo);

            if (!alreadyViewed) {
                userActivityRepository.save(UserActivity.builder()
                        .user(user)
                        .work(work)
                        .activityType(ActivityType.VIEW) // ✅ 자동으로 VIEW 설정
                        .build());

                work.increaseViewCount();
            }

        }
        return work.getViewCount();
    }

    @Transactional
    public int recordLike(Long workId, Long userId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException("Resource not found","해당 작품을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Resource not found","해당 사용자를 찾을 수 없습니다."));

        //조회하는 사람이 일반 유저일 때만~ 조회수 올리기
        if (user.getUserRole().toString().equals("USER")) {

            // ✅ 중복 좋아요 방지 로직 (CustomException 사용)
            boolean alreadyLiked = userActivityRepository.isCurrentlyLiked(user, work);
            if (alreadyLiked) {
                throw new CustomException("Resource already exists", "이미 좋아요를 한 작품 입니다.");
            }
            // ✅ 좋아요 추가
            userActivityRepository.save(UserActivity.builder()
                    .user(user)
                    .work(work)
                    .activityType(ActivityType.LIKE) // ✅ 좋아요 수 증가
                    .build());

            work.increaseLikeCount();
        }
        return work.getLikeCount();
    }

    @Transactional
    public void recordUnlike(Long workId, Long userId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new CustomException("Resource not found", "해당 작품을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Resource not found", "해당 사용자를 찾을 수 없습니다."));

        //우선은 방어코드를 넣어보았다.
        //조회하는 사람이 일반 유저일 때만~ 조회수 올리기
        if (user.getUserRole().toString().equals("USER")) {
            // ✅ 좋아요 여부 확인
            boolean liked = userActivityRepository.isCurrentlyLiked(user, work);
            if (!liked) {
                throw new CustomException("Resource not found", "해당 작품을 좋아요하지 않아서 좋아요 취소를 할 수 없습니다.");
            }

            // ✅ 새로운 "좋아요 취소" 기록 추가
            userActivityRepository.save(UserActivity.builder()
                    .user(user)
                    .work(work)
                    .activityType(ActivityType.UNLIKE)  // ✅ "UNLIKE" 추가
                    .build());

            work.decreaseLikeCount();  // ✅ 좋아요 수 감소
        }
    }
}

