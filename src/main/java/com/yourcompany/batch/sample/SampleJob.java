package com.yourcompany.batch.sample;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
            .start(stepFactory.getStep(TransactionStepBuilder.class))
            .next(stepFactory.getStep(SampleStepBuilder.class))
            .next(stepFactory.getStepTasklet(SampleTasklet.class))
            .listener(jobCompletionListener)
            .build();
    }

    /**
     * Scheduled job - chạy mỗi 5 phút (có thể tùy chỉnh)
     * @SchedulerLock đảm bảo chỉ có 1 instance chạy job tại một thời điểm
     * Comment lại nếu không muốn chạy tự động
     */
    // @Scheduled(fixedDelay = 300000) // 5 phút
    // @Scheduled(cron = "0 */5 * * * ?") // Mỗi 5 phút
    @SchedulerLock(
        name = "SAMPLE_JOB_SCHEDULER_LOCK",
        lockAtMostFor = "10m",  // Lock tối đa 10 phút (nếu job crash, lock sẽ tự động release sau 10 phút)
        lockAtLeastFor = "5m"   // Lock tối thiểu 5 phút (tránh chạy quá thường xuyên)
    )
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

