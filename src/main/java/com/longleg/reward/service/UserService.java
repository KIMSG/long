package com.longleg.reward.service;

import com.longleg.reward.entity.*;
import com.longleg.reward.exception.CustomException;
import com.longleg.reward.repository.RewardHistoryRepository;
import com.longleg.reward.repository.UserActivityRepository;
import com.longleg.reward.repository.UserRepository;
import com.longleg.reward.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final RewardHistoryRepository rewardHistoryRepository;
    private final UserActivityRepository userActivityRepository;

    private final UserRepository userRepository;
    private final UserActivityService userActivityService;
    private final WorkRepository workRepository;
    private final RewardService rewardService;

    /**
     * 특정 사용자의 리워드 내역을 조회하여 지급 사유를 포함한 정보를 반환하는 메서드
     */
    public Map<String, Object> getUserReward(Long id) {
        Map<String, Object> response = new HashMap<>();

        // 사용자의 리워드 내역을 조회 (지급 요청일과 함께 조회)
        List<RewardHistoryDTO> rewardHistories = rewardHistoryRepository.findByReceiverIdWithRequestDate(id)
                .stream()
                .map(obj -> new RewardHistoryDTO((RewardHistory) obj[0], (LocalDate) obj[1]))
                .toList();

        // 리워드 내역을 가공하여 최종 반환할 리스트
        List<RewardHistoryDTO> rewardHistories1 = new ArrayList<>();
        for (RewardHistoryDTO rewardHistoryDTO : rewardHistories) {
            LocalDate rewardDate = rewardHistoryDTO.getRequestDate();

            // 해당 지급 요청 날짜에 대한 상위 작품 활동 데이터 조회
            List<WorkActivityDTO> topWorks = rewardService.reasonReward(rewardDate);

            List<WorkActivityDTO> reason = new ArrayList<>();
            int rank = 1;
            for (WorkActivityDTO workActivityDTO : topWorks) {
                workActivityDTO.setRank(rank);

                // 사용자 정보를 조회 (예외 발생 가능)
                User user = userRepository.findById(id)
                        .orElseThrow(() -> new CustomException("Resource not found","해당 사용자를 찾을 수 없습니다."));

                // 작품 정보를 조회 (예외 발생 가능)
                Work work = workRepository.findById(workActivityDTO.getWorkId())
                        .orElseThrow(() -> new CustomException("Resource not found","해당 작품을 찾을 수 없습니다."));

                if (user.getUserRole().toString().equals("AUTHOR")) {
                    /*사용자가 작가(Author)인 경우
                    * 자신의 작품에 대해서 지급사유를 저장
                    * */
                    if (work.getAuthor().getId().equals(id)){
                        reason.add(workActivityDTO);
                        rewardHistoryDTO.setRewardReason(reason);
                        rewardHistories1.add(rewardHistoryDTO);
                        break;
                    }
                }else {

                    /*
                     * 사용자가 일반 소비자인 경우
                     * 특정 작품에서 조건을 충족한 사용자인지 확인
                     * (좋아요 / 조회수)
                     */
                    List<Long> userIds = userActivityService.getQualifiedUsers(workActivityDTO.getWorkId(), rewardDate);
                    for (Long userId : userIds) {
                        if (userId.equals(id)){
                            reason.add(workActivityDTO);
                            break;
                        }
                    }
                }
                rewardHistoryDTO.setRewardReason(reason);
                rank++;
            }
            rewardHistories1.add(rewardHistoryDTO);
        }

        response.put("rewards", rewardHistories1); // 원하는 키
        response.put("totalCount", rewardHistories.size()); // 총 개수 추가

        return response;
    }
}
