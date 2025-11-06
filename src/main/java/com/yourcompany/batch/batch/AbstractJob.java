package com.yourcompany.batch.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import com.yourcompany.batch.batch.listener.JobCompletionListener;
import com.yourcompany.batch.batch.step.StepFactory;
import com.yourcompany.batch.config.ApplicationProperties;
import com.yourcompany.batch.config.Constants;
import com.yourcompany.batch.service.JobSettingService;
import com.yourcompany.batch.service.dto.JobSettingDTO;

import java.io.IOException;
import java.util.*;

public abstract class AbstractJob {
    private static final Logger log = LoggerFactory.getLogger(AbstractJob.class);

    protected LinkedHashMap<String, Object> params;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    protected JobCompletionListener jobCompletionListener;

    @Autowired
    protected JobLauncher jobLauncher;

    @Autowired
    protected JobRepository jobRepository;

    @Autowired
    protected JobSettingService jobSettingService;

    @Autowired
    protected StepFactory stepFactory;

    @Autowired
    protected JobOperator jobOperator;

    @Autowired
    private ObjectMapper objectMapper;

    public abstract String getJobName();

    protected abstract Job job();

    protected void beforeJobRun() {}

    public void run(LinkedHashMap<String, Object> params) {
        beforeJobRun();
        MDC.put(Constants.CORRELATION_ID_LOG_VAR_NAME, UUID.randomUUID().toString().toUpperCase().replace("-", ""));
        this.params = params;
        
        Optional<JobSettingDTO> optJobSetting = jobSettingService.findByJobName(getJobName());
        if (optJobSetting.isPresent()) {
            log.info("{} setting with status {}", getJobName(), optJobSetting.get().getStatus());
            JobSettingDTO jobSetting = optJobSetting.get();
            if (StringUtils.isNotBlank(jobSetting.getParams())) {
                TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<>() {};
                try {
                    LinkedHashMap<String, Object> maps = objectMapper.readValue(jobSetting.getParams(), typeRef);
                    if (maps != null && !maps.isEmpty()) {
                        if (params == null || params.isEmpty()) {
                            this.params = maps;
                        } else {
                            this.params = new LinkedHashMap<>(maps);
                            params.forEach((key, value) -> this.params.merge(key, value, (v1, v2) -> v2));
                        }
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            log.info("{} have no setting", getJobName());
        }
        
        if (optJobSetting.isPresent() && !(Integer.valueOf(1).equals(optJobSetting.get().getStatus()))) {
            log.info("{} setting no run at : {}", getJobName(), new Date());
        } else {
            log.info("{} Started at : {}", getJobName(), new Date());
            JobParameters param = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
            try {
                JobExecution execution = jobLauncher.run(job(), param);
                log.info("{} finished with status : {}", getJobName(), execution.getStatus());
            } catch (JobExecutionAlreadyRunningException | JobRestartException | 
                     JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
                log.error(getJobName(), e);
            }
        }
        MDC.remove(Constants.CORRELATION_ID_LOG_VAR_NAME);
    }

    public void run() {
        run(null);
    }

    @Async
    public void runAsync() {
        this.run();
    }

    public void runBySchedule(LinkedHashMap<String, Object> params) {
        if (applicationProperties.getSchedule().isEnabled()) {
            this.run(params);
        } else {
            log.warn("Job Schedule setting enabled: {}", applicationProperties.getSchedule().isEnabled());
        }
    }

    public void runBySchedule() {
        runBySchedule(null);
    }

    protected JobBuilder createJobBuilder(String jobName) {
        return new JobBuilder(jobName, jobRepository);
    }
}

