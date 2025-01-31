package com.longleg.reward.service;

import com.longleg.reward.entity.*;
import com.longleg.reward.exception.CustomException;
import com.longleg.reward.repository.*;
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
    private final WorkRepository workRepository;
    private final RewardHistoryRepository rewardHistoryRepository;
    private final UserActivityService userActivityService;;
    private final UserRepository userRepository;

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

        // ✅ 응답 데이터 생성
        return createResponse("Reward request completed.", rewardDate, "COMPLETE" ,topWorks);
    }

    @Transactional
    public Map<String, Object> rewardExecute(LocalDate rewardDate) {
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

        /*작가에게는 리워드가 한번만 지급되어야 한다
        * A작가 작품이 1등 / 10등 이면 1등의 리워드 100, 10등의 리워드 10  이렇게 해서 합하여 110을 지급하기로함.
        * */
        int score = 100;
        for (WorkActivityDTO workActivityDTO : topWorks) {

            // 1. 작품 정보 조회 (workId가 유효한지 확인)
            Work work = workRepository.findById(workActivityDTO.getWorkId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 작품이 존재하지 않습니다."));

            // 2. 작가 정보 가져오기
            User author = work.getAuthor();

            // 3. 리워드 지급 요청 조회
            RewardRequest rewardRequest = rewardRequestRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("리워드 지급 요청을 찾을 수 없습니다."));

            // 4. `RewardHistory` 객체 생성 및 저장
            RewardHistory history = new RewardHistory(rewardRequest, author, work, score);
            score = score-10;
            rewardHistoryRepository.save(history);

        }

        // ✅ 유저 ID별 점수를 저장할 Map (userId -> score)
        List<Map<String, Object>> userScoreList = new ArrayList<>();

        for (WorkActivityDTO workActivityDTO : topWorks) {
            // 1. 작품 정보 조회 (workId가 유효한지 확인)
            Work work = workRepository.findById(workActivityDTO.getWorkId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 작품이 존재하지 않습니다."));

            List<Long> userIds = userActivityService.getQualifiedUsers(work.getId());

            for (Long userId : userIds) {
                boolean found = false;

                // 기존에 존재하는 값이 있는지 확인
                for (Map<String, Object> entry : userScoreList) {
                    if (entry.get("userId").equals(userId)) {
                        entry.put("currentScore", (Integer) entry.get("currentScore") + 1);
                        found = true;
                        break;
                    }
                }

                // 없으면 새로 추가
                if (!found) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("workId", work.getId());
                    entry.put("userId", userId);
                    entry.put("currentScore", 1);
                    userScoreList.add(entry);
                }
            }
        }

        for (Map<String, Object> entry : userScoreList) {
            Long userId = ((Number) entry.get("userId")).longValue();
            int userScore = ((Number) entry.get("currentScore")).intValue();

            User consumer = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(userId + " : 해당 소비자가 존재하지 않습니다."));

            RewardHistory history = new RewardHistory(request, consumer, null, userScore);
            rewardHistoryRepository.save(history);
        }

        distributeRewards(request.getId());

        return createResponse("Reward request completed.", rewardDate, "COMPLETE" ,topWorks);
    }

    @Transactional
    public void distributeRewards(Long rewardRequestId) {
        // 1. 특정 rewardRequestId에 해당하는 미지급 보상 조회
        List<Object[]> unpaidRewards = rewardHistoryRepository.findUnpaidRewardsByRequest(rewardRequestId);

        for (Object[] record : unpaidRewards) {
            Long userId = ((Number) record[0]).longValue();
            int totalPoints = ((Number) record[1]).intValue();

            // 2. 사용자의 reward 값 업데이트
            userRepository.updateUserReward(userId, totalPoints);

            // 3. 해당 유저의 reward_history 기록을 paid 처리
            rewardHistoryRepository.markAsPaid(userId, rewardRequestId);
        }
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

