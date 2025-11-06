package com.yourcompany.batch.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public abstract class AbstractJobExecutionListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJobExecutionListener.class);

    @Override
    public final void beforeJob(JobExecution jobExecution) {
        logger.info("BATCH JOB ID {} STARTING...", jobExecution.getJobId());
        this.executeBeforeJob(jobExecution);
    }

    @Override
    public final void afterJob(JobExecution jobExecution) {
        logger.info("BATCH JOB ID {} START FROM {} TO {} COMPLETED WITH STATUS {}", 
            jobExecution.getJobId(), jobExecution.getStartTime(), jobExecution.getEndTime(), jobExecution.getStatus());
        this.executeAfterJob(jobExecution);
    }

    public void executeBeforeJob(JobExecution jobExecution) {
        // Override if needed
    }

    public void executeAfterJob(JobExecution jobExecution) {
        // Override if needed
    }
}

