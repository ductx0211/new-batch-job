package com.yourcompany.batch.batch.tasklet;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.LinkedHashMap;

public abstract class AbstractTasklet implements Tasklet, StepExecutionListener {

    protected abstract String getName();

    @Autowired
    protected JobRepository jobRepository;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    private LinkedHashMap<String, Object> parameters;

    public LinkedHashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(LinkedHashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Step buildStep() {
        return new StepBuilder(getName(), jobRepository)
            .tasklet(this, transactionManager)
            .listener(this)
            .build();
    }

    // StepExecutionListener methods - có thể override nếu cần
    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Override nếu cần
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // Override nếu cần
        return stepExecution.getExitStatus();
    }
}

