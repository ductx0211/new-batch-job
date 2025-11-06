package com.yourcompany.batch.service;

import com.yourcompany.batch.domain.JobSetting;
import com.yourcompany.batch.repository.JobSettingRepository;
import com.yourcompany.batch.service.dto.JobSettingDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class JobSettingService {

    @Autowired
    protected JobSettingRepository jobSettingRepository;

    @Transactional(readOnly = true)
    public Optional<JobSettingDTO> findByJobName(String jobName) {
        return jobSettingRepository.findById(jobName)
            .map(this::toDto);
    }

    private JobSettingDTO toDto(JobSetting jobSetting) {
        JobSettingDTO dto = new JobSettingDTO();
        BeanUtils.copyProperties(jobSetting, dto);
        return dto;
    }
}

