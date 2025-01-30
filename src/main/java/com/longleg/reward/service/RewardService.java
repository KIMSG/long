package com.longleg.reward.service;

import com.longleg.reward.entity.RewardRequest;
import com.longleg.reward.entity.Work;
import com.longleg.reward.entity.WorkActivityDTO;
import com.longleg.reward.entity.WorkActivityProjection;
import com.longleg.reward.exception.CustomException;
import com.longleg.reward.repository.RewardRequestRepository;
import com.longleg.reward.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRequestRepository rewardRequestRepository;
    private final UserActivityRepository userActivityRepository;

    /**
     * 리워드 지급 요청을 처리하는 메서드
     */
    @Transactional
    public Map<String, Object> processReward(LocalDate rewardDate) {
        validateRewardRequest(rewardDate);

        // ✅ 리워드 지급 요청 저장
        RewardRequest request = new RewardRequest(rewardDate);
        rewardRequestRepository.save(request);
        request.complete();
        rewardRequestRepository.save(request);

        // ✅ 응답 데이터 생성
        return createResponse("리워드 지급 요청이 완료되었습니다.", rewardDate, request.getStatus().toString(), null);
    }

    /**
     * 특정 날짜의 상위 10개 작품을 기반으로 리워드를 계산하는 메서드
     */
    @Transactional
    public Map<String, Object> calReward(LocalDate rewardDate) {

        // ✅ 작품별 활동 데이터 조회 후 DTO 변환
        List<WorkActivityDTO> rankedWorks = userActivityRepository.getWorkActivityCounts()
                .stream()
                .map(row -> new WorkActivityDTO(row.getWorkId(), row.getLikeCount(), row.getViewCount()))
                .sorted(Comparator.comparingInt(WorkActivityDTO::getScore).reversed()) // ✅ 점수 기준 내림차순 정렬
                .toList();

        // ✅ 상위 10개만 선택
        List<WorkActivityDTO> topWorks = rankedWorks.stream().limit(10).toList();

        // ✅ 리워드 지급 요청 저장
        RewardRequest request = new RewardRequest(rewardDate);
        rewardRequestRepository.save(request);
        request.complete();
        rewardRequestRepository.save(request);

        // ✅ 응답 데이터 생성
        return createResponse("Reward request completed.", rewardDate, request.getStatus().toString(), topWorks);
    }

    /**
     * 리워드 지급 요청이 유효한지 검증하는 메서드
     */
    private void validateRewardRequest(LocalDate rewardDate) {
        LocalDate today = LocalDate.now();

        if (!rewardDate.isBefore(today)) {
            throw new CustomException("Reward can't be requested", "요청 당일과 미래 날짜는 선택할 수 없습니다.");
        }

        if (rewardRequestRepository.findByRequestDate(rewardDate).isPresent()) {
            throw new CustomException("Reward already requested", rewardDate + " 일은 이미 리워드 지급 요청이 있습니다.");
        }
    }

    /**
     * 응답 데이터를 생성하는 메서드
     */
    private Map<String, Object> createResponse(String message, LocalDate rewardDate, String status, List<WorkActivityDTO> topWorks) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("requestDate", rewardDate.toString());
        response.put("status", status);
        if (topWorks != null) {
            response.put("topWorks", topWorks);
        }
        return response;
    }




}

