package com.longleg.reward.repository;

import com.longleg.reward.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorksRepository extends JpaRepository<Work, Long> {
}
