package com.yourcompany.batch.batch.step;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import com.yourcompany.batch.domain.JobLog;
import com.yourcompany.batch.domain.JobLogResult;
import com.yourcompany.batch.domain.enumeration.LogTypeEnum;
import com.yourcompany.batch.repository.JobLogRepository;
import com.yourcompany.batch.repository.JobLogResultRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractStepBuilder<I, O> {

    private static final Logger log = LoggerFactory.getLogger(AbstractStepBuilder.class);
    private static final int CHUNK_SIZE = 1000;
    private static final int SKIP_LIMIT = 99;
    private static final int CHUNK_PROCESS_LOG_SIZE = 1000;

    private AtomicInteger numberOfProcessed = new AtomicInteger(-1);
    private AtomicInteger numberOfFailures = new AtomicInteger(-1);
    private int totalItems = -1;
    private Instant startTime;
    private Instant endTime;
    final Class<I> inputClass;
    private String jobName;
    private Long stepExecutionId;
    private Long jobExecutionId;
    private String noteResult;
    private LinkedHashMap<String, Object> parameters;

    @Autowired
    protected org.springframework.batch.core.repository.JobRepository jobRepository;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Autowired
    private JobLogResultRepository jobLogResultRepository;

    @SuppressWarnings("unchecked")
    public AbstractStepBuilder() {
        Type t = getClass().getGenericSuperclass();
        while (!(t instanceof ParameterizedType)) {
            t = ((Class<?>) t).getGenericSuperclass();
        }
        ParameterizedType pt = (ParameterizedType) t;
        this.inputClass = (Class<I>) pt.getActualTypeArguments()[0];
    }

    public LinkedHashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(LinkedHashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    protected int getNumberOfProcessed() {
        return this.numberOfProcessed.get();
    }

    protected int getNumberOfFailures() {
        return this.numberOfFailures.get();
    }

    protected int getChunkSize() {
        return CHUNK_SIZE;
    }

    protected abstract int countTotalItems();

    public final int getTotalItems() {
        return this.totalItems;
    }

    protected abstract String getStepName();

    protected String getJobName() {
        return this.jobName;
    }

    protected void beforeStep(StepExecution stepExecution) {}

    protected void afterStep(StepExecution stepExecution) {}

    protected String getLogMessageAtStart() {
        return null;
    }

    protected abstract ItemReader<I> reader();

    protected abstract ItemProcessor<I, O> processor();

    protected abstract ItemWriter<O> writer();

    protected void setNoteResult(String note) {
        this.noteResult = note;
    }

    protected Long getStepExecutionId() {
        return this.stepExecutionId;
    }

    protected Long getJobExecutionId() {
        return this.jobExecutionId;
    }

    protected final void addJobLog(LogTypeEnum logType, String message) {
        addJobLog(logType, message, null);
    }

    protected final void addJobLog(LogTypeEnum logType, String message, String note) {
        addJobLog(getStepName(), logType, message, note);
    }

    protected final void addJobError(String message, Throwable throwable) {
        addJobLog(getStepName(), LogTypeEnum.ERROR, message, ExceptionUtils.getStackTrace(throwable));
    }

    private void addJobLog(String stepName, LogTypeEnum logType, String message, String note) {
        JobLog jobLog = new JobLog();
        jobLog.setJobName(stepName);
        jobLog.setLogType(logType);
        jobLog.setMessage(message);
        jobLog.setNote(note);
        jobLog.setStepExecutionId(stepExecutionId);
        jobLog.setJobExecutionId(jobExecutionId);
        jobLog.setCreatedBy(getJobName());
        jobLog.setCreatedDate(Instant.now());
        jobLogRepository.save(jobLog);
    }

    public Step build() {
        return new StepBuilder(this.getStepName(), jobRepository)
            .<I, O>chunk(getChunkSize(), transactionManager)
            .reader(reader())
            .processor(processor())
            .writer(writer())
            .listener(new org.springframework.batch.core.ChunkListener() {
                int numItemPerLog = 0;

                @Override
                public void beforeChunk(ChunkContext context) {
                    // WebSocket notification if needed
                }

                @Override
                public void afterChunk(ChunkContext context) {
                    try {
                        long writeCount = context.getStepContext().getStepExecution().getWriteCount();
                        int processed = numberOfProcessed.addAndGet((int) writeCount);
                        numItemPerLog += (int) writeCount;
                        if (numItemPerLog >= CHUNK_PROCESS_LOG_SIZE || writeCount < getChunkSize()) {
                            addJobLog(LogTypeEnum.COUNT, "Count: " + processed + "/" + totalItems, String.valueOf(getParameters()));
                            numItemPerLog = 0;
                        }
                    } catch (Exception ex) {
                        log.error("Error in afterChunk", ex);
                    }
                }

                @Override
                public void afterChunkError(ChunkContext context) {
                    Throwable throwable = context.getStepContext().getStepExecution().getFailureExceptions().isEmpty() 
                        ? null 
                        : context.getStepContext().getStepExecution().getFailureExceptions().get(0);
                    if (throwable != null) {
                        numberOfFailures.incrementAndGet();
                        log.error("AbstractStepBuilder.afterChunkError", throwable);
                        String message = throwable.getMessage();
                        if (message != null && message.length() > 4000) {
                        message = message.substring(0, 4000);
                    }
                        addJobError(message, throwable);
                    }
                }
            })
            .faultTolerant()
            .skipLimit(getSkipLimit())
            .skip(Exception.class)
            .noSkip(IOException.class)
            .listener(new org.springframework.batch.core.SkipListener<I, O>() {
                @Override
                public void onSkipInRead(Throwable t) {
                    log.error("AbstractStepBuilder.onSkipInRead", t);
                    String message = t.getMessage();
                    if (message != null && message.length() > 4000) {
                        message = message.substring(0, 4000);
                    }
                    addJobError(message, t);
                }

                @Override
                @Transactional
                public void onSkipInWrite(O item, Throwable t) {
                    log.error("AbstractStepBuilder.onSkipInWrite", t);
                    numberOfFailures.decrementAndGet();
                    String message = t.getMessage();
                    if (message != null && message.length() > 4000) {
                        message = message.substring(0, 4000);
                    }
                    addJobError(message, t);
                }

                @Override
                public void onSkipInProcess(I item, Throwable t) {
                    log.error("AbstractStepBuilder.onSkipInProcess", t);
                    String message = t.getMessage();
                    if (message != null && message.length() > 4000) {
                        message = message.substring(0, 4000);
                    }
                    addJobError(message, t);
                }
            })
            .listener(new StepExecutionListener() {
                @Override
                @Transactional
                public void beforeStep(StepExecution stepExecution) {
                    log.info("BATCH JOB STEP NAME {} - BATCH JOB EXECUTION ID {} STARTING...", 
                        stepExecution.getStepName(), stepExecution.getJobExecutionId());
                    numberOfProcessed = new AtomicInteger(0);
                    numberOfFailures = new AtomicInteger(0);
                    startTime = Instant.now();
                    jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
                    stepExecutionId = stepExecution.getId();
                    jobExecutionId = stepExecution.getJobExecutionId();
                    AbstractStepBuilder.this.beforeStep(stepExecution);
                    if (!stepExecution.isTerminateOnly()) {
                        totalItems = countTotalItems();
                        if (noteResult == null) {
                            noteResult = "Total: " + totalItems;
                        }
                    } else {
                        if (noteResult == null) {
                            noteResult = "Terminate Step";
                        }
                    }
                    addJobLog(LogTypeEnum.START, getLogMessageAtStart(), "Total: " + totalItems + ", " + getParameters());
                }

                @Override
                @Transactional
                public ExitStatus afterStep(StepExecution stepExecution) {
                    endTime = Instant.now();
                    addJobLog(LogTypeEnum.END, "Count: " + numberOfProcessed.get() + "/" + totalItems, String.valueOf(getParameters()));
                    String status = stepExecution.getStatus() != null ? stepExecution.getStatus().name() : null;

                    log.info("BATCH JOB STEP NAME {} - BATCH JOB EXECUTION ID {} COMPLETED WITH STATUS {}", 
                        stepExecution.getStepName(), stepExecution.getJobExecutionId(), status);

                    AbstractStepBuilder.this.afterStep(stepExecution);

                    addJobResult(getStepName(), numberOfProcessed.get(), numberOfFailures.get(),
                        startTime, endTime, noteResult, totalItems, 
                        (int) stepExecution.getReadSkipCount(),
                        (int) stepExecution.getProcessSkipCount(),
                        (int) stepExecution.getWriteSkipCount(),
                        (int) stepExecution.getSkipCount(),
                        SKIP_LIMIT, status);

                    return ExitStatus.COMPLETED;
                }
            })
            .build();
    }

    private void addJobResult(String stepName, Integer rowRun, Integer rowError, 
                             Instant startTime, Instant endTime, String note, Integer total,
                             Integer skipReader, Integer skipProcessor, Integer skipWriter, 
                             Integer skipTotal, Integer skipLimit, String status) {
        JobLogResult jobLogResult = new JobLogResult();
        jobLogResult.setJobName(stepName);
        jobLogResult.setRowRun(rowRun);
        jobLogResult.setRowError(rowError);
        jobLogResult.setStartTime(startTime);
        jobLogResult.setEndTime(endTime);
        jobLogResult.setNote(note);
        jobLogResult.setTotal(total);
        jobLogResult.setSkipReader(skipReader);
        jobLogResult.setSkipProcessor(skipProcessor);
        jobLogResult.setSkipWriter(skipWriter);
        jobLogResult.setSkipTotal(skipTotal);
        jobLogResult.setSkipLimit(skipLimit);
        jobLogResult.setStepExecutionId(stepExecutionId);
        jobLogResult.setJobExecutionId(jobExecutionId);
        jobLogResult.setStatus(status);
        jobLogResult.setCreatedBy(getJobName());
        jobLogResult.setCreatedDate(Instant.now());
        jobLogResultRepository.save(jobLogResult);
    }

    public void beforeChunk(ChunkContext chunkContext) {}
    public void afterChunk(ChunkContext chunkContext) {}
    public void afterChunkError(ChunkContext chunkContext) {}

    public int getSkipLimit() {
        return SKIP_LIMIT;
    }
}

