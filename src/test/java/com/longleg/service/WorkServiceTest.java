package com.longleg.service;

import com.longleg.entity.*;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @DisplayName("recordUnlike - 좋아요를 취소하는 경우 정상 처리")
    void recordUnlike_Success() {
        // Given
        given(workRepository.findById(workId)).willReturn(Optional.of(work));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userActivityRepository.isCurrentlyLiked(user, work)).willReturn(true); // ✅ 좋아요한 상태
        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);
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

        given(user.getUserRole()).willReturn(com.longleg.entity.UserRole.USER);
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> workService.recordUnlike(workId, userId));
        assertThat(exception.getMessage()).isEqualTo("해당 작품을 좋아요하지 않아서 좋아요 취소를 할 수 없습니다.");

        verify(userActivityRepository, never()).save(any(UserActivity.class));
        verify(work, never()).decreaseLikeCount();
    }


    @Test
    void recordView_WhenWorkNotFound_ShouldThrowException() {
        // given
        Long workId = 999L; // 존재하지 않는 ID
        Long userId = 1L;

        when(workRepository.findById(workId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            workService.recordView(workId, userId);
        });

        assertEquals("Resource not found", exception.getError());
        assertEquals("해당 작품을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void recordView_WhenUserNotFound_ShouldThrowException() {
        // given
        Long workId = 1L;
        Long userId = 999L; // 존재하지 않는 사용자 ID

        Work work = mock(Work.class);
        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            workService.recordView(workId, userId);
        });

        assertEquals("Resource not found", exception.getError());
        assertEquals("해당 사용자를 찾을 수 없습니다.", exception.getMessage());
    }


    @Test
    void recordLike_WhenUserAlreadyLiked_ShouldThrowException() {
        // given
        Long workId = 1L;
        Long userId = 1L;

        Work work = mock(Work.class);
        User user = mock(User.class);

        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserRole()).thenReturn(UserRole.USER); // ✅ 일반 사용자로 설정

        when(userActivityRepository.isCurrentlyLiked(user, work)).thenReturn(true); // ✅ 이미 좋아요한 상태

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            workService.recordLike(workId, userId);
        });

        assertEquals("Resource already exists", exception.getError());
        assertEquals("이미 좋아요를 한 작품입니다.", exception.getMessage());

        verify(work, never()).increaseLikeCount(); // ✅ 좋아요 수 증가하지 않아야 함
    }

    @Test
    void recordUnlike_WhenUserIsRegularUser_ShouldDecreaseLikeCount() {
        // given
        Long workId = 1L;
        Long userId = 1L;

        Work work = mock(Work.class);
        User user = mock(User.class);

        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserRole()).thenReturn(UserRole.USER); // ✅ 일반 사용자 설정

        when(userActivityRepository.isCurrentlyLiked(user, work)).thenReturn(true); // ✅ 좋아요한 상태

        // when
        workService.recordUnlike(workId, userId);

        // then
        verify(userActivityRepository, times(1)).save(any()); // ✅ "UNLIKE" 기록 저장 확인
        verify(work, times(1)).decreaseLikeCount(); // ✅ 좋아요 수 감소 확인
    }

    @Test
    void recordUnlike_WhenUserIsAdmin_ShouldNotDecreaseLikeCount() {
        // given
        Long workId = 1L;
        Long userId = 2L;

        Work work = mock(Work.class);
        User user = mock(User.class);

        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserRole()).thenReturn(UserRole.AUTHOR); // ✅ 관리자 설정

        // when
        workService.recordUnlike(workId, userId);

        // then
        verify(userActivityRepository, never()).save(any()); // ✅ 좋아요 취소 기록이 저장되지 않아야 함
        verify(work, never()).decreaseLikeCount(); // ✅ 좋아요 수 감소되지 않아야 함
    }
    @Test
    void recordLike_WhenUserIsAdmin_ShouldNotIncreaseLikeCount() {
        // given
        Long workId = 1L;
        Long userId = 2L;

        Work work = mock(Work.class);
        User user = mock(User.class);

        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserRole()).thenReturn(UserRole.AUTHOR); // ✅ 관리자 설정

        // when
        int likeCount = workService.recordLike(workId, userId);

        // then
        verify(userActivityRepository, never()).save(any()); // ✅ 좋아요 기록이 저장되지 않아야 함
        verify(work, never()).increaseLikeCount(); // ✅ 좋아요 수 증가하지 않아야 함
    }

    @Test
    void recordLike_WhenUserIsAdmin_ShouldNotIncreaseViewCount() {
        // given
        Long workId = 1L;
        Long userId = 2L;

        Work work = mock(Work.class);
        User user = mock(User.class);

        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserRole()).thenReturn(UserRole.AUTHOR); // ✅ 관리자 설정

        // when
        int likeCount = workService.recordLike(workId, userId);

        // then
        verify(userActivityRepository, never()).save(any()); // ✅ 좋아요 기록이 저장되지 않아야 함
        verify(work, never()).increaseLikeCount(); // ✅ 좋아요 수 증가하지 않아야 함
    }


}
