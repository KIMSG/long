package com.longleg.service;

import com.longleg.entity.RewardHistory;
import com.longleg.entity.RewardRequest;
import com.longleg.entity.User;
import com.longleg.entity.Work;
import com.longleg.repository.*;
import com.longleg.dto.WorkActivityDTO;
import com.longleg.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @InjectMocks
    private RewardService rewardService;

    @Mock
    private RewardRequestRepository rewardRequestRepository;

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private RewardHistoryRepository rewardHistoryRepository;

    @Mock
    private UserActivityService userActivityService;

    @Mock
    private UserRepository userRepository;

    private LocalDate rewardDate;

    @BeforeEach
    void setUp() {
        rewardDate = LocalDate.now().minusDays(1);
    }

}
