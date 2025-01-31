package com.longleg.repository;

import com.longleg.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorksRepository extends JpaRepository<Work, Long> {
}
