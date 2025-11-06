package com.yourcompany.batch.repository;

import com.yourcompany.batch.domain.JobSetting;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobSettingRepository extends JpaRepository<JobSetting, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select js from JobSetting js where js.jobName = :jobName")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "60000")})
    JobSetting findByJobNameForWrite(@Param("jobName") String jobName);
}

