package com.yourcompany.batch.sample;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.yourcompany.batch.batch.AbstractJob;
import com.yourcompany.batch.batch.listener.JobCompletionListener;
import com.yourcompany.batch.batch.step.StepFactory;

/**
 * Sample Job để demo cách sử dụng framework
 */
@Component
public class SampleJob extends AbstractJob {

    private static final String JOB_NAME = "SAMPLE_JOB";

    @Autowired
    private StepFactory stepFactory;

    @Autowired
    private JobCompletionListener jobCompletionListener;

    @Override
    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    protected Job job() {
        return createJobBuilder(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .start(stepFactory.getStep(SampleStepBuilder.class))
            .next(stepFactory.getStepTasklet(SampleTasklet.class))
            .listener(jobCompletionListener)
            .build();
    }

    /**
     * Scheduled job - chạy mỗi 5 phút (có thể tùy chỉnh)
     * Comment lại nếu không muốn chạy tự động
     */
    // @Scheduled(fixedDelay = 300000) // 5 phút
    // @Scheduled(cron = "0 */5 * * * ?") // Mỗi 5 phút
    public void schedule() {
        runBySchedule();
    }

    /**
     * Chạy job thủ công
     */
    public void runManual() {
        run();
    }
}

