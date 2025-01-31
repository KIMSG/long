package com.longleg.service;

import com.longleg.entity.RewardHistory;
import com.longleg.entity.RewardRequest;
import com.longleg.entity.User;
import com.longleg.entity.Work;
import com.longleg.repository.*;
import com.longleg.dto.WorkActivityDTO;
import com.longleg.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

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
     * 특정 날짜의 상위 10개 작품을 기반으로 리워드를 계산하는 메서드
     */
    @Transactional
    public Map<String, Object> calReward(LocalDate rewardDate) {
        LocalDate today = LocalDate.now();

        if (!rewardDate.isBefore(today)) {
            throw new CustomException("Reward can't be requested", "요청 당일과 미래 날짜는 선택할 수 없습니다.");
        }
        List<WorkActivityDTO> topWorks = reasonReward(rewardDate);

        // 응답 데이터 생성
        return createResponse("Reward request completed.", rewardDate, "COMPLETE" ,topWorks);
    }

    /**
     * 리워드 지급 요청을 처리하는 메서드
     */
    @Transactional
    public Map<String, Object> rewardExecute(LocalDate rewardDate) {

        validateRewardRequest(rewardDate);

        List<WorkActivityDTO> topWorks = reasonReward(rewardDate);

        // 리워드 지급 요청 저장
        RewardRequest request = new RewardRequest(rewardDate);
        rewardRequestRepository.save(request);
        
        //리워드 지급 진행중
        request.startProcessing();
        rewardRequestRepository.save(request);

        allocateAuthorRewards(request, topWorks);

        // 유저 ID별 점수를 저장할 Map (userId -> score)
        List<Map<String, Object>> userScoreList = distributeAuthorRankingRewards(topWorks, rewardDate);

        distributeConsumerRankingRewards(request, userScoreList);

        distributeRewards(request.getId());

        //랭킹 지급 계산 후 지급 요청건에 대한 상태값 변경
        request.complete();
        rewardRequestRepository.save(request);

        return createResponse("Reward request completed.", rewardDate, "COMPLETE" ,topWorks);
    }

    /**
     * 상위 랭킹 작품의 작가들에게 리워드를 지급하는 메소드
     *
     * 상위 랭킹 작품(topWorks) 작가(author) 정보를 가져옴
     * 특정 rewardRequestId에 해당하는 리워드 지급 요청 정보 조회
     * `RewardHistory` 객체를 생성하여 지급 내역을 저장
     * 상위 순위에 따라 지급되는 리워드 점수를 감소시키며 저장
     *
     * @param request  리워드 지급 요청 정보 (RewardRequest)
     * @param topWorks 상위 랭킹에 포함된 작품 리스트 (WorkActivityDTO)
     */
    @Transactional
    public void allocateAuthorRewards(RewardRequest request, List<WorkActivityDTO> topWorks){
        int score = 100;
        for (WorkActivityDTO workActivityDTO : topWorks) {

            // 1. 작품 정보 조회 (workId가 유효한지 확인)
            Work work = workRepository.findById(workActivityDTO.getWorkId())
                    .orElseThrow(() -> new CustomException("Resource not found","해당 작품이 존재하지 않습니다."));

            // 2. 작가 정보 가져오기
            User author = work.getAuthor();

            // 3. 리워드 지급 요청 조회
            RewardRequest rewardRequest = rewardRequestRepository.findById(request.getId())
                    .orElseThrow(() -> new CustomException("Resource not found","리워드 지급 요청을 찾을 수 없습니다."));

            // 4. `RewardHistory` 객체 생성 및 저장
            RewardHistory history = new RewardHistory(rewardRequest, author, work, score);
            score = score-10;
            rewardHistoryRepository.save(history);

        }
    }


    /**
     * 사용자 랭킹 리워드를 계산하는 메소드
     *
     * 상위 작품 리스트(topWorks) 각 작품의 정보를 조회
     * 해당 작품(workId)과 연관된 사용자(기여자) 목록을 조회
     * 조회된 사용자(userId)에 대해 보상 점수를 계산하여 userScoreList에 저장
     * 동일 사용자(userId)가 이미 존재하는 경우, 점수를 누적하여 업데이트
     * 최종적으로 각 사용자(userId)별 보상 점수를 포함한 리스트(userScoreList)를 반환
     *
     * @param topWorks 상위 랭킹에 포함된 작품 리스트 (WorkActivityDTO)
     * @return 각 사용자(userId)별 보상 점수를 포함한 리스트
     */
    @Transactional
    public  List<Map<String, Object>> distributeAuthorRankingRewards(List<WorkActivityDTO> topWorks,LocalDate rewardDate ) {
        List<Map<String, Object>> userScoreList = new ArrayList<>();
        for (WorkActivityDTO workActivityDTO : topWorks) {
            // 1. 작품 정보 조회 (workId가 유효한지 확인)
            Work work = workRepository.findById(workActivityDTO.getWorkId())
                    .orElseThrow(() -> new CustomException("Resource not found","해당 작품이 존재하지 않습니다."));

            List<Long> userIds = userActivityService.getQualifiedUsers(work.getId(), rewardDate);

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
        return userScoreList;
    }

    /**
     * 소비자 랭킹 리워드를 지급하는 메소드
     *
     * userScoreList를 순회하며 각 소비자(userId)의 점수를 확인
     * userId에 해당하는 소비자 정보를 조회
     * reward_history 테이블에 보상 내역을 저장
     *
     * @param request       현재 지급할 리워드 요청 정보 (RewardRequest)
     * @param userScoreList 소비자 ID(userId)와 해당 사용자의 점수(currentScore)가 포함된 리스트
     */
    @Transactional
    public void distributeConsumerRankingRewards(RewardRequest request, List<Map<String, Object>> userScoreList) {
        for (Map<String, Object> entry : userScoreList) {
            Long userId = ((Number) entry.get("userId")).longValue();
            int userScore = ((Number) entry.get("currentScore")).intValue();

            User consumer = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("Resource not found", "해당 소비자가 존재하지 않습니다."));

            RewardHistory history = new RewardHistory(request, consumer, null, userScore);
            rewardHistoryRepository.save(history);
        }
    }

    /**
     * 특정 rewardRequestId에 해당하는 사용자들에게 리워드를 지급하는 메소드
     *
     * 특정 rewardRequestId에 해당하는 미지급 보상을 조회
     * 조회된 사용자(userId)의 reward 값을 업데이트
     * 해당 보상이 지급되었음을 기록하기 위해 reward_history의 total_paid 값을 true로 변경
     *
     * @param rewardRequestId 지급할 리워드 요청 ID
     */
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
     * 특정 날짜의 상위 10개 작품을 정렬하여 저장
     */
    public List<WorkActivityDTO> reasonReward(LocalDate rewardDate) {
        // 작품별 활동 데이터 조회 후 DTO 변환
        List<WorkActivityDTO> rankedWorks = Optional.ofNullable(userActivityRepository.getWorkActivityCounts(rewardDate))
                .orElseThrow(() -> new CustomException("Resource not found","활동 데이터 조회 결과가 null입니다.")) // ✅ null 체크
                .stream()
                .map(row -> new WorkActivityDTO(row.getWorkId(), row.getLikeCount(), row.getViewCount(), row.getUserId()))
                .sorted(Comparator.comparingInt(WorkActivityDTO::getScore).reversed()) // 점수 기준 내림차순 정렬
                .toList();

        // 상위 10개만 선택
        List<WorkActivityDTO> topWorks = rankedWorks.stream().limit(10).toList();

        return topWorks;
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

