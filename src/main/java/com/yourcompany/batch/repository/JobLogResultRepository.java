package com.yourcompany.batch.repository;

import com.yourcompany.batch.domain.JobLogResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogResultRepository extends JpaRepository<JobLogResult, Long> {
}

