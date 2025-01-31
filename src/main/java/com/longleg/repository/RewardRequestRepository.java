package com.longleg.repository;

import com.longleg.entity.RewardRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RewardRequestRepository extends JpaRepository<RewardRequest, Long> {
    Optional<RewardRequest> findByRequestDate(LocalDate createdAt);

}

