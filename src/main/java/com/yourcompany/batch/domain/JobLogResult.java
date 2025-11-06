package com.yourcompany.batch.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "job_log_result")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class JobLogResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    @Column(name = "job_name", length = 50)
    private String jobName;

    @Column(name = "row_run")
    private Integer rowRun;

    @Column(name = "row_error")
    private Integer rowError;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Size(max = 4000)
    @Column(name = "note", length = 4000)
    private String note;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "total")
    private Integer total;

    @Column(name = "SKIP_READER")
    private Integer skipReader;

    @Column(name = "SKIP_PROCESSOR")
    private Integer skipProcessor;

    @Column(name = "SKIP_WRITER")
    private Integer skipWriter;

    @Column(name = "SKIP_TOTAL")
    private Integer skipTotal;

    @Column(name = "SKIP_LIMIT")
    private Integer skipLimit;

    @Column(name = "STEP_EXECUTION_ID")
    private Long stepExecutionId;

    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;

    @Column(name = "status")
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getRowRun() {
        return rowRun;
    }

    public void setRowRun(Integer rowRun) {
        this.rowRun = rowRun;
    }

    public Integer getRowError() {
        return rowError;
    }

    public void setRowError(Integer rowError) {
        this.rowError = rowError;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSkipReader() {
        return skipReader;
    }

    public void setSkipReader(Integer skipReader) {
        this.skipReader = skipReader;
    }

    public Integer getSkipProcessor() {
        return skipProcessor;
    }

    public void setSkipProcessor(Integer skipProcessor) {
        this.skipProcessor = skipProcessor;
    }

    public Integer getSkipWriter() {
        return skipWriter;
    }

    public void setSkipWriter(Integer skipWriter) {
        this.skipWriter = skipWriter;
    }

    public Integer getSkipTotal() {
        return skipTotal;
    }

    public void setSkipTotal(Integer skipTotal) {
        this.skipTotal = skipTotal;
    }

    public Integer getSkipLimit() {
        return skipLimit;
    }

    public void setSkipLimit(Integer skipLimit) {
        this.skipLimit = skipLimit;
    }

    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JobLogResult)) {
            return false;
        }
        return id != null && id.equals(((JobLogResult) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}

