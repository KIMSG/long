package com.longleg.reward.service;

import com.longleg.reward.repository.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;

    @Autowired
    public UserActivityService(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    public List<Long> getQualifiedUsers(Long workId) {
        return userActivityRepository.findQualifiedUserIds(workId);
    }
}
