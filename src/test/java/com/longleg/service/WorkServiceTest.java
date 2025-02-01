package com.longleg.service;

import com.longleg.entity.ActivityType;
import com.longleg.entity.User;
import com.longleg.entity.UserActivity;
import com.longleg.entity.Work;
import com.longleg.exception.CustomException;
import com.longleg.repository.UserActivityRepository;
import com.longleg.repository.UserRepository;
import com.longleg.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkService 단위 테스트")
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActivityRepository userActivityRepository;

    @InjectMocks
    private WorkService workService;

    private Long workId;
    private Long userId;
    private User user;
    private Work work;

    @BeforeEach
    void setUp() {
        workId = 100L;
        userId = 1L;
        user = Mockito.mock(User.class);
        work = Mockito.mock(Work.class);

        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);

//        user = Mockito.mock(User.class);
//        given(user.getId()).willReturn(userId);
//        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);
//
//        work = Mockito.mock(Work.class);
//        given(work.getId()).willReturn(workId);
//        given(work.getViewCount()).willReturn(10);
//        given(work.getLikeCount()).willReturn(5);
    }

    @Test
    @DisplayName("recordView - 일반 사용자의 조회 기록 정상 저장")
    void recordView_Success() {
        // Given
        given(workRepository.findById(workId)).willReturn(Optional.of(work));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);
        given(userActivityRepository.existsByUserAndWorkAndCreatedAtAfter(any(User.class), any(Work.class), any(LocalDateTime.class)))
                .willReturn(false); // ✅ 1시간 내에 조회한 적 없음

        // ✅ 초기 조회수 설정
        given(work.getViewCount()).willReturn(9); // 기존 조회수

        // ✅ `increaseViewCount()`가 호출되면 `getViewCount()`가 10을 반환하도록 설정
        doAnswer(invocation -> {
            given(work.getViewCount()).willReturn(10);
            return null;
        }).when(work).increaseViewCount();

        // When
        int result = workService.recordView(workId, userId);

        // Then
        assertThat(result).isEqualTo(10); // ✅ 올바른 조회수 검증
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(work).increaseViewCount();
    }

    @Test
    @DisplayName("recordView - 1시간 내에 조회한 경우 조회 기록 저장 안 됨")
    void recordView_AlreadyViewed() {
        // Given
        given(workRepository.findById(workId)).willReturn(Optional.of(work));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);

        given(userActivityRepository.existsByUserAndWorkAndCreatedAtAfter(any(User.class), any(Work.class), any(LocalDateTime.class)))
                .willReturn(true); // ✅ 1시간 내에 이미 조회 기록 있음

        // ✅ 초기 조회수 설정
        given(work.getViewCount()).willReturn(10);

        // When
        int result = workService.recordView(workId, userId);

        // Then
        assertThat(result).isEqualTo(10); // ✅ 올바른 조회수 검증
        verify(userActivityRepository, never()).save(any(UserActivity.class)); // ✅ 조회 기록이 저장되지 않아야 함
        verify(work, never()).increaseViewCount(); // ✅ 조회수 증가 메서드가 호출되지 않아야 함

    }

    @Test
    @DisplayName("recordLike - 일반 사용자의 좋아요 정상 저장")
    void recordLike_Success() {
        // Given
        given(workRepository.findById(workId)).willReturn(Optional.of(work));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);
        given(userActivityRepository.isCurrentlyLiked(user, work)).willReturn(false); // ✅ 좋아요한 적 없음

        // ✅ 좋아요 개수를 명시적으로 설정
        given(work.getLikeCount()).willReturn(5);

        // When
        int result = workService.recordLike(workId, userId);

        // Then
        assertThat(result).isEqualTo(5); // ✅ 올바른 좋아요 개수 검증
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(work).increaseLikeCount();
    }

    @Test
    @DisplayName("recordLike - 이미 좋아요한 경우 예외 발생")
    void recordLike_AlreadyLiked() {
        // Given
        given(workRepository.findById(workId)).willReturn(Optional.of(work));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // ✅ user.getUserRole() 명시적으로 설정 (Null 방지)
        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);

        given(userActivityRepository.isCurrentlyLiked(user, work)).willReturn(true); // ✅ 이미 좋아요한 상태

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> workService.recordLike(workId, userId));
        assertThat(exception.getMessage()).isEqualTo("이미 좋아요를 한 작품 입니다.");

        verify(userActivityRepository, never()).save(any(UserActivity.class));
        verify(work, never()).increaseLikeCount();
    }

    @Test
    @DisplayName("recordUnlike - 좋아요를 취소하는 경우 정상 처리")
    void recordUnlike_Success() {
        // Given
        given(workRepository.findById(workId)).willReturn(Optional.of(work));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userActivityRepository.isCurrentlyLiked(user, work)).willReturn(true); // ✅ 좋아요한 상태

        // When
        workService.recordUnlike(workId, userId);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(work).decreaseLikeCount();
    }

    @Test
    @DisplayName("recordUnlike - 좋아요하지 않은 작품을 취소할 때 예외 발생")
    void recordUnlike_NotLiked() {
        // Given
        given(workRepository.findById(workId)).willReturn(Optional.of(work));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userActivityRepository.isCurrentlyLiked(user, work)).willReturn(false); // ✅ 좋아요하지 않은 상태

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> workService.recordUnlike(workId, userId));
        assertThat(exception.getMessage()).isEqualTo("해당 작품을 좋아요하지 않아서 좋아요 취소를 할 수 없습니다.");

        verify(userActivityRepository, never()).save(any(UserActivity.class));
        verify(work, never()).decreaseLikeCount();
    }


}
