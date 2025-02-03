package com.longleg.service;

import com.longleg.dto.RewardHistoryDTO;
import com.longleg.dto.WorkActivityDTO;
import com.longleg.entity.RewardHistory;
import com.longleg.entity.User;
import com.longleg.entity.UserRole;
import com.longleg.entity.Work;
import com.longleg.exception.CustomException;
import com.longleg.repository.RewardHistoryRepository;
import com.longleg.repository.UserRepository;
import com.longleg.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private RewardHistoryRepository rewardHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private RewardService rewardService;

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private UserService userService;

    private User user;
    private Work work;
    private RewardHistory rewardHistory;
    private LocalDate requestDate;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUserRole(UserRole.AUTHOR);

        work = new Work();
        work.setId(100L);
        work.setAuthor(user);

        rewardHistory = new RewardHistory();
        requestDate = LocalDate.now();
    }

    @Test
    void testGetUserReward_Success() {
        RewardHistory rewardHistory = new RewardHistory();
        LocalDate requestDate = LocalDate.now();

        when(rewardHistoryRepository.findByReceiverIdWithRequestDate(any()))
                .thenReturn(List.of(new Object[][]{ {rewardHistory, requestDate} }));


        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(workRepository.findById(any())).thenReturn(Optional.of(work));
        when(rewardService.reasonReward(any())).thenReturn(List.of(new WorkActivityDTO(100L)));

        Map<String, Object> result = userService.getUserReward(1L);

        assertNotNull(result);
        assertEquals(1, result.get("totalCount"));
    }

    @Test
    void testGetUserReward_InvalidDataFormat() {
        // 잘못된 데이터 (RewardHistory, LocalDate가 아님)
        List<Object[]> invalidData = new ArrayList<>();
        invalidData.add(new Object[]{"InvalidData", 123});

        when(rewardHistoryRepository.findByReceiverIdWithRequestDate(any()))
                .thenReturn(invalidData);

        CustomException exception = assertThrows(CustomException.class, () -> userService.getUserReward(1L));

        assertEquals("해당 작품을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void testGetUserReward_UserAsConsumer() {
        RewardHistory rewardHistory = new RewardHistory();
        LocalDate requestDate = LocalDate.now();

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{rewardHistory, requestDate});

        when(rewardHistoryRepository.findByReceiverIdWithRequestDate(any()))
                .thenReturn(mockData);


        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(workRepository.findById(any())).thenReturn(Optional.of(work));
        when(rewardService.reasonReward(any())).thenReturn(List.of(new WorkActivityDTO(100L)));

        // 🔹 사용자의 역할을 일반 소비자로 설정
        user.setUserRole(UserRole.USER);

        // 🔹 userActivityService.getQualifiedUsers(...)가 특정 사용자 ID를 포함한 리스트를 반환하도록 설정
        when(userActivityService.getQualifiedUsers(any(), any()))
                .thenReturn(List.of(1L)); // ID 1번 사용자가 조건 충족

        Map<String, Object> result = userService.getUserReward(1L);

        assertNotNull(result);
        assertEquals(1, result.get("totalCount"));
    }

    @Test
    void getUserReward_shouldProcessValidRewardHistory() {
        // given: 정상적인 RewardHistory + LocalDate
        RewardHistory mockRewardHistory = mock(RewardHistory.class);
        List<Object[]> rawResults = new ArrayList<>();
        rawResults.add(new Object[]{ mockRewardHistory, LocalDate.now() });

        when(rewardHistoryRepository.findByReceiverIdWithRequestDate(userId)).thenReturn(rawResults);

        // when & then
        Map<String, Object> result = userService.getUserReward(userId);
        assertNotNull(result.get("rewards")); // 정상적으로 반환되는지 확인
    }

    @Test
    void getUserReward_shouldThrowExceptionForInvalidRewardHistoryObject() {
        // given: obj[0]이 RewardHistory가 아닌 경우
        List<Object[]> rawResults = new ArrayList<>();
        rawResults.add(new Object[]{ "InvalidObject", LocalDate.now() });

        when(rewardHistoryRepository.findByReceiverIdWithRequestDate(userId)).thenReturn(rawResults);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.getUserReward(userId);
        });

        assertEquals("해당 작품을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void getUserReward_shouldThrowExceptionForInvalidLocalDateObject() {
        // given: obj[1]이 LocalDate가 아닌 경우
        RewardHistory mockRewardHistory = mock(RewardHistory.class);
        List<Object[]> rawResults = new ArrayList<>();
        rawResults.add(new Object[]{ mockRewardHistory, "InvalidDate" });

        when(rewardHistoryRepository.findByReceiverIdWithRequestDate(userId)).thenReturn(rawResults);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.getUserReward(userId);
        });

        assertEquals("해당 작품을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void getUserReward_shouldThrowExceptionForShortObjectArray() {
        // given: obj.length < 2 인 경우
        RewardHistory mockRewardHistory = mock(RewardHistory.class);
        List<Object[]> rawResults = new ArrayList<>();
        rawResults.add(new Object[]{ mockRewardHistory }); // 길이가 1개만 있음

        when(rewardHistoryRepository.findByReceiverIdWithRequestDate(userId)).thenReturn(rawResults);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.getUserReward(userId);
        });

        assertEquals("해당 작품을 찾을 수 없습니다.", exception.getMessage());
    }


}
