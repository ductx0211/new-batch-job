package com.yourcompany.batch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yourcompany.batch.sample.SampleJob;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller để trigger jobs
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final SampleJob sampleJob;

    public JobController(SampleJob sampleJob) {
        this.sampleJob = sampleJob;
    }

    /**
     * Trigger Sample Job
     */
    @PostMapping("/sample/run")
    public ResponseEntity<Map<String, Object>> runSampleJob() {
        Map<String, Object> response = new HashMap<>();
        try {
            sampleJob.runManual();
            response.put("status", "success");
            response.put("message", "Sample job started successfully");
            response.put("jobName", "SAMPLE_JOB");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to start sample job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get job status
     */
    @GetMapping("/sample/status")
    public ResponseEntity<Map<String, String>> getSampleJobStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("jobName", "SAMPLE_JOB");
        response.put("status", "available");
        response.put("description", "Sample job for demonstration");
        return ResponseEntity.ok(response);
    }
}

