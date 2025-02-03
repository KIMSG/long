package com.longleg.service;

import com.longleg.dto.WorkActivityDTO;
import com.longleg.entity.*;
import com.longleg.exception.CustomException;
import com.longleg.repository.RewardRequestRepository;
import com.longleg.repository.UserActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.longleg.repository.*;


@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Spy
    @InjectMocks
    private RewardService rewardService;

    @Mock
    private UserActivityRepository userActivityRepository; // 추가된 부분

    @Mock
    private RewardRequestRepository rewardRequestRepository;

    @Mock
    private RewardHistoryRepository rewardHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private UserActivityService userActivityService;

    private Work work;
    private WorkActivityDTO workActivityDTO;
    private RewardRequest rewardRequest;

    @BeforeEach
    void setUp() {
        work = new Work();
        work.setId(1L);
        rewardRequest = new RewardRequest(1L);
        workActivityDTO = new WorkActivityDTO(1L);
    }
    @Test
    void calReward_ShouldThrowException_WhenRewardDateIsTodayOrFuture() {
        LocalDate today = LocalDate.now();

        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.calReward(today);
        });

        assertEquals("요청 당일과 미래 날짜는 선택할 수 없습니다.", exception.getMessage());
    }
    @Test
    void calReward_ShouldThrowException_WhenRewardDateIsFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.calReward(futureDate);
        });

        assertEquals("요청 당일과 미래 날짜는 선택할 수 없습니다.", exception.getMessage());
    }

    @Test
    void calReward_ShouldPass_WhenRewardDateIsPast() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        Map<String, Object> response = rewardService.calReward(pastDate);

        assertNotNull(response);
        assertEquals("COMPLETE", response.get("status"));
    }

    @Test
    void reasonReward_ShouldThrowException_WhenDataIsNull() {
        LocalDate rewardDate = LocalDate.now().minusDays(1);

        when(userActivityRepository.getWorkActivityCounts(rewardDate)).thenReturn(null); // null 반환

        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.reasonReward(rewardDate);
        });

        assertEquals("활동 데이터 조회 결과가 null입니다.", exception.getMessage());
    }

    @Test
    void reasonReward_ShouldReturnSortedTop10_WhenValidDataIsGiven() {
        LocalDate rewardDate = LocalDate.now().minusDays(1);

        // WorkActivityProjection을 Mock 객체로 생성
        WorkActivityProjection projection1 = Mockito.mock(WorkActivityProjection.class);
        WorkActivityProjection projection2 = Mockito.mock(WorkActivityProjection.class);

        // 각 Projection의 동작을 정의 (Mock 객체가 특정 값 반환)
        when(projection1.getWorkId()).thenReturn(1L);
        when(projection1.getLikeCount()).thenReturn(5);
        when(projection1.getViewCount()).thenReturn(100);
        when(projection1.getUserId()).thenReturn(10L);

        when(projection2.getWorkId()).thenReturn(2L);
        when(projection2.getLikeCount()).thenReturn(10);
        when(projection2.getViewCount()).thenReturn(200);
        when(projection2.getUserId()).thenReturn(20L);

        List<WorkActivityProjection> mockData = List.of(projection1, projection2);

        // userActivityRepository.getWorkActivityCounts()가 List<WorkActivityProjection>을 반환해야 함
        when(userActivityRepository.getWorkActivityCounts(rewardDate)).thenReturn(mockData);

        List<WorkActivityDTO> result = rewardService.reasonReward(rewardDate);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getScore() >= result.get(1).getScore());
    }

    @Test
    void validateRewardRequest_ShouldThrowException_WhenDateIsTodayOrFuture() {
        LocalDate today = LocalDate.now();

        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.calReward(today);
        });

        assertEquals("요청 당일과 미래 날짜는 선택할 수 없습니다.", exception.getMessage());
    }


    @Test
    void distributeRewards_ShouldUpdateUserRewardsAndMarkAsPaid() {
        Long rewardRequestId = 1L;

        // ✅ 1. Mock 데이터 설정 (미지급 보상 조회)
        List<Object[]> unpaidRewards = List.of(
                new Object[]{1001L, 50},  // 유저 ID: 1001, 보상 포인트: 50
                new Object[]{1002L, 75}   // 유저 ID: 1002, 보상 포인트: 75
        );

        when(rewardHistoryRepository.findUnpaidRewardsByRequest(rewardRequestId)).thenReturn(unpaidRewards);

        // ✅ 2. 메서드 실행
        rewardService.distributeRewards(rewardRequestId);

        // ✅ 3. `updateUserReward()`가 올바르게 호출되었는지 검증
        verify(userRepository, times(1)).updateUserReward(1001L, 50);
        verify(userRepository, times(1)).updateUserReward(1002L, 75);

        // ✅ 4. `markAsPaid()`가 올바르게 호출되었는지 검증
        verify(rewardHistoryRepository, times(1)).markAsPaid(1001L, rewardRequestId);
        verify(rewardHistoryRepository, times(1)).markAsPaid(1002L, rewardRequestId);
    }


    @Test
    void distributeConsumerRankingRewards_ShouldSaveRewardHistoryForValidUsers() {
        // ✅ 1. Mock 데이터 설정
        RewardRequest mockRequest = Mockito.mock(RewardRequest.class);

        List<Map<String, Object>> userScoreList = List.of(
                Map.of("userId", 1001L, "currentScore", 50),
                Map.of("userId", 1002L, "currentScore", 75)
        );

        User user1 = Mockito.mock(User.class);
        User user2 = Mockito.mock(User.class);

        when(userRepository.findById(1001L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(1002L)).thenReturn(Optional.of(user2));

        // ✅ 2. 메서드 실행
        rewardService.distributeConsumerRankingRewards(mockRequest, userScoreList);

        // ✅ 3. `findById()`가 두 번 호출되었는지 검증
        verify(userRepository, times(1)).findById(1001L);
        verify(userRepository, times(1)).findById(1002L);

        // ✅ 4. `rewardHistoryRepository.save()`가 두 번 호출되었는지 검증
        verify(rewardHistoryRepository, times(2)).save(any(RewardHistory.class));
    }

    @Test
    void distributeConsumerRankingRewards_ShouldThrowException_WhenUserNotFound() {
        // ✅ 1. Mock 데이터 설정
        RewardRequest mockRequest = Mockito.mock(RewardRequest.class);

        List<Map<String, Object>> userScoreList = List.of(
                Map.of("userId", 1001L, "currentScore", 50)
        );

        when(userRepository.findById(1001L)).thenReturn(Optional.empty()); // 존재하지 않는 유저

        // ✅ 2. 예외 발생 검증
        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.distributeConsumerRankingRewards(mockRequest, userScoreList);
        });

        assertEquals("해당 소비자가 존재하지 않습니다.", exception.getMessage());

        // ✅ 3. `rewardHistoryRepository.save()`는 호출되지 않아야 함
        verify(rewardHistoryRepository, never()).save(any(RewardHistory.class));
    }

    @Test
    void shouldThrowException_WhenRewardDateIsTodayOrFuture() {
        LocalDate today = LocalDate.now();

        assertThrows(CustomException.class, () -> rewardService.calReward(today)); // ✅ `calReward()`가 `validateRewardRequest()`를 호출하므로 테스트 가능
    }

    @Test
    void distributeAuthorRankingRewards_Success() {
        LocalDate rewardDate = LocalDate.now();
        List<WorkActivityDTO> topWorks = List.of(new WorkActivityDTO(1L)); // workId가 null이 아님을 보장

        when(workRepository.findById(any())).thenReturn(Optional.of(work));
        when(userActivityService.getQualifiedUsers(anyLong(), any()))
                .thenReturn(List.of(100L, 101L, 100L));  // 동일한 userId(100L) 두 번 등장


        List<Map<String, Object>> result = rewardService.distributeAuthorRankingRewards(topWorks, rewardDate);

        assertNotNull(result);
        assertEquals(2, result.size());
    }


    @Test
    void distributeAuthorRankingRewards_WorkNotFound() {
        LocalDate rewardDate = LocalDate.now();
        List<WorkActivityDTO> topWorks = List.of(new WorkActivityDTO(1L)); // workId가 null이 아님을 보장

        when(workRepository.findById(any())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.distributeAuthorRankingRewards(topWorks, rewardDate);
        });

        assertEquals("해당 작품이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void allocateAuthorRewards_Success() {
        List<WorkActivityDTO> topWorks = List.of(new WorkActivityDTO(1L)); // workId가 null이 아님을 보장

        when(workRepository.findById(any())).thenReturn(Optional.of(work));
        when(rewardRequestRepository.findById(any())).thenReturn(Optional.of(rewardRequest));

        rewardService.allocateAuthorRewards(rewardRequest, topWorks);

        verify(rewardHistoryRepository, times(1)).save(any(RewardHistory.class));
    }

    @Test
    void allocateAuthorRewards_WorkNotFound() {
        List<WorkActivityDTO> topWorks = List.of(workActivityDTO);

        when(workRepository.findById(any())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.allocateAuthorRewards(rewardRequest, topWorks);
        });

        assertEquals("해당 작품이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void allocateAuthorRewards_RewardRequestNotFound() {
        List<WorkActivityDTO> topWorks = List.of(workActivityDTO);

        when(workRepository.findById(any())).thenReturn(Optional.of(work)); // 작품이 정상 조회됨
        when(rewardRequestRepository.findById(any())).thenReturn(Optional.empty()); // 리워드 요청 없음

        CustomException exception = assertThrows(CustomException.class, () -> {
            rewardService.allocateAuthorRewards(rewardRequest, topWorks);
        });

        assertEquals("리워드 지급 요청을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void createResponse_ShouldNotIncludeTopWorks_WhenTopWorksIsNull() {
        LocalDate rewardDate = LocalDate.now().minusDays(1);

        when(rewardService.reasonReward(rewardDate)).thenReturn(Collections.emptyList()); // ✅ 빈 리스트 반환

        Map<String, Object> response = rewardService.calReward(rewardDate); // 내부적으로 createResponse() 실행됨

        assertNotNull(response);
        assertEquals("COMPLETE", response.get("status"));
        assertFalse(response.containsKey("topWorks")); // ✅ `topWorks` 키 자체가 없어야 함


    }

    @Test
    void testRewardExecute() {
        // Given
        LocalDate rewardDate = LocalDate.of(2024, 2, 1);
        RewardRequest request = new RewardRequest(rewardDate);

        List<WorkActivityDTO> topWorks = Arrays.asList(
                new WorkActivityDTO(1L),
                new WorkActivityDTO(2L)
        );
        List<Map<String, Object>> userScoreList = List.of(
                Map.of("userId", 1L, "score", 100),
                Map.of("userId", 2L, "score", 80)
        );


        // `save()` 호출 시 ID를 설정하여 `null` 방지
        when(rewardRequestRepository.save(any())).thenAnswer(invocation -> {
            RewardRequest savedRequest = invocation.getArgument(0);
            savedRequest.setId(1L); // ID 값 할당
            return savedRequest;
        });

        doNothing().when(rewardService).validateRewardRequest(any(LocalDate.class));
        when(rewardService.reasonReward(any(LocalDate.class))).thenReturn(topWorks);

        // ArgumentMatchers를 활용하여 정확한 매칭 문제 해결
        doNothing().when(rewardService).allocateAuthorRewards(any(RewardRequest.class), anyList());
        when(rewardService.distributeAuthorRankingRewards(anyList(), any(LocalDate.class))).thenReturn(userScoreList);
        doNothing().when(rewardService).distributeConsumerRankingRewards(any(RewardRequest.class), anyList());
        // `distributeRewards()`가 `null`을 받을 수 있도록 수정
        doNothing().when(rewardService).distributeRewards(nullable(Long.class));


        // When
        Map<String, Object> response = rewardService.rewardExecute(rewardDate);

        // Then
        assertNotNull(response);
        assertEquals("Reward request completed.", response.get("message"));
        assertEquals("COMPLETE", response.get("status"));

        verify(rewardRequestRepository, times(3)).save(any(RewardRequest.class));
        verify(rewardService, times(1)).validateRewardRequest(any(LocalDate.class));
        verify(rewardService, times(1)).reasonReward(any(LocalDate.class));
        verify(rewardService, times(1)).allocateAuthorRewards(any(RewardRequest.class), anyList());
        verify(rewardService, times(1)).distributeAuthorRankingRewards(anyList(), any(LocalDate.class));
        verify(rewardService, times(1)).distributeConsumerRankingRewards(any(RewardRequest.class), anyList());
        verify(rewardService, times(1)).distributeRewards(nullable(Long.class));
    }


}
