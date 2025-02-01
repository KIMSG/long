package com.longleg.service;

import com.longleg.dto.RewardHistoryDTO;
import com.longleg.dto.WorkActivityDTO;
import com.longleg.entity.RewardHistory;
import com.longleg.entity.User;
import com.longleg.entity.UserRole;
import com.longleg.entity.Work;
import com.longleg.exception.CustomException;
import com.longleg.repository.RewardHistoryRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito 확장 적용
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private RewardHistoryRepository rewardHistoryRepository;

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActivityService userActivityService;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private UserService userService;

    private Long userId;
    private Long workId;
    private LocalDate rewardDate;
    private RewardHistory rewardHistory;
    private User user;
    private Work work;
    private WorkActivityDTO workActivityDTO;
    private RewardHistoryDTO rewardHistoryDTO;

    @BeforeEach
    void setUp() {
        userId = 1L;
        workId = 100L;
        rewardDate = LocalDate.of(2024, 2, 1);

        // ✅ Mock 객체 생성
        user = Mockito.mock(User.class);
        given(user.getId()).willReturn(userId);
        given(user.getUserRole()).willReturn(UserRole.AUTHOR); // ✅ Mock 객체의 값 지정

        work = Mockito.mock(Work.class);
        given(work.getId()).willReturn(100L);
        given(work.getAuthor()).willReturn(user); // ✅ Mock 객체의 값 지정

        rewardHistory = Mockito.mock(RewardHistory.class);
        rewardHistoryDTO = new RewardHistoryDTO(rewardHistory, rewardDate);

        workActivityDTO = new WorkActivityDTO();
        workActivityDTO.setWorkId(work.getId());

        // ✅ lenient()를 사용하여 불필요한 stubbing 경고 무시
//        lenient().when(userActivityService.getQualifiedUsers(any(Long.class), any(LocalDate.class)))
//                .thenReturn(List.of(userId));
    }

    @Test
    @DisplayName("getUserReward - 정상적인 리워드 내역 반환")
    void getUserReward_ReturnsRewardHistory() {
        // Given
        given(rewardHistoryRepository.findByReceiverIdWithRequestDate(userId))
                .willReturn(List.of(new Object[][]{{rewardHistory, rewardDate}}));

        given(user.getId()).willReturn(userId);
        given(user.getUserRole()).willReturn(UserRole.AUTHOR); // ✅ Mock 객체의 값 지정
        given(work.getId()).willReturn(100L);
        given(work.getAuthor()).willReturn(user); // ✅ Mock 객체의 값 지정

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(workRepository.findById(work.getId())).willReturn(Optional.of(work));
        given(rewardService.reasonReward(rewardDate)).willReturn(List.of(workActivityDTO));

        // When
        Map<String, Object> result = userService.getUserReward(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("rewards")).isInstanceOf(List.class);
        assertThat(result.get("totalCount")).isEqualTo(1);

        verify(rewardHistoryRepository).findByReceiverIdWithRequestDate(userId);
        verify(userRepository).findById(userId);
        verify(workRepository).findById(work.getId());
        verify(rewardService).reasonReward(rewardDate);
    }

}