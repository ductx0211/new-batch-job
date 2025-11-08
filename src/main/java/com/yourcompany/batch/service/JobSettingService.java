package com.yourcompany.batch.service;

import com.yourcompany.batch.domain.JobSetting;
import com.yourcompany.batch.repository.JobSettingRepository;
import com.yourcompany.batch.service.dto.JobSettingDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    @Transactional
    public JobSettingDTO saveOrUpdate(JobSettingDTO dto) {
        JobSetting jobSetting = jobSettingRepository.findById(dto.getJobName())
            .orElse(new JobSetting());
        
        jobSetting.setJobName(dto.getJobName());
        jobSetting.setDescription(dto.getDescription());
        jobSetting.setStatus(dto.getStatus());
        jobSetting.setParams(dto.getParams());
        jobSetting.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : "SYSTEM");
        jobSetting.setUpdatedDate(Instant.now());
        
        if (jobSetting.getCreatedDate() == null) {
            jobSetting.setCreatedDate(Instant.now());
            jobSetting.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM");
        }
        
        jobSetting = jobSettingRepository.save(jobSetting);
        return toDto(jobSetting);
    }

    @Transactional
    public JobSettingDTO updateParams(String jobName, String params, String updatedBy) {
        JobSetting jobSetting = jobSettingRepository.findById(jobName)
            .orElse(new JobSetting());
        
        jobSetting.setJobName(jobName);
        jobSetting.setParams(params);
        jobSetting.setUpdatedBy(updatedBy != null ? updatedBy : "SYSTEM");
        jobSetting.setUpdatedDate(Instant.now());
        
        if (jobSetting.getCreatedDate() == null) {
            jobSetting.setCreatedDate(Instant.now());
            jobSetting.setCreatedBy(updatedBy != null ? updatedBy : "SYSTEM");
        }
        
        jobSetting = jobSettingRepository.save(jobSetting);
        return toDto(jobSetting);
    }

    private JobSettingDTO toDto(JobSetting jobSetting) {
        JobSettingDTO dto = new JobSettingDTO();
        BeanUtils.copyProperties(jobSetting, dto);
        return dto;
    }
}

