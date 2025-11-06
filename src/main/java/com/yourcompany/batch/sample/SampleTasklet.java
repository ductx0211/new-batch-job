package com.yourcompany.batch.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import com.yourcompany.batch.batch.tasklet.AbstractTasklet;

/**
 * Sample Tasklet để demo cách sử dụng AbstractTasklet
 * Tasklet này thực hiện một tác vụ đơn giản: log thông tin
 */
@Component
public class SampleTasklet extends AbstractTasklet {

    private static final Logger log = LoggerFactory.getLogger(SampleTasklet.class);

    @Override
    protected String getName() {
        return "SAMPLE_TASKLET";
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Sample Tasklet executing...");
        
        // Lấy parameters nếu có
        if (getParameters() != null && !getParameters().isEmpty()) {
            log.info("Tasklet parameters: {}", getParameters());
        }
        
        // Thực hiện logic của tasklet
        // Ví dụ: gửi email, gọi API, xử lý file, etc.
        log.info("Tasklet logic: Processing sample task");
        
        // Simulate some work
        Thread.sleep(1000);
        
        log.info("Sample Tasklet completed");
        
        return RepeatStatus.FINISHED;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        super.beforeStep(stepExecution);
        log.info("Sample Tasklet - beforeStep");
    }

    @Override
    public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Sample Tasklet - afterStep");
        return super.afterStep(stepExecution);
    }
}

