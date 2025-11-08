package com.yourcompany.batch.controller;

import com.yourcompany.batch.service.JobSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để quản lý Transaction Job configuration
 * Cho phép thay đổi pageSize động mà không cần restart app
 */
@RestController
@RequestMapping("/api/transaction-job")
public class TransactionJobController {

    @Autowired
    private JobSettingService jobSettingService;

    /**
     * Lấy config hiện tại của Transaction Job
     * GET /api/transaction-job/config
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> response = new HashMap<>();
        
        jobSettingService.findByJobName("TRANSACTION_PROCESSING_STEP")
            .ifPresentOrElse(
                jobSetting -> {
                    response.put("jobName", jobSetting.getJobName());
                    response.put("description", jobSetting.getDescription());
                    response.put("status", jobSetting.getStatus());
                    response.put("params", jobSetting.getParams());
                    response.put("message", "Config found");
                },
                () -> {
                    response.put("message", "No config found for TRANSACTION_PROCESSING_STEP");
                    response.put("defaultPageSize", 10);
                }
            );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật pageSize cho Transaction Job
     * PUT /api/transaction-job/config/page-size
     * Body: {"pageSize": 20}
     */
    @PutMapping("/config/page-size")
    public ResponseEntity<Map<String, Object>> updatePageSize(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object pageSizeObj = request.get("pageSize");
            if (pageSizeObj == null) {
                response.put("error", "pageSize is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            int pageSize;
            if (pageSizeObj instanceof Number) {
                pageSize = ((Number) pageSizeObj).intValue();
            } else if (pageSizeObj instanceof String) {
                pageSize = Integer.parseInt((String) pageSizeObj);
            } else {
                response.put("error", "pageSize must be a number");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (pageSize <= 0) {
                response.put("error", "pageSize must be greater than 0");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Tạo params JSON
            String params = String.format("{\"pageSize\": %d}", pageSize);
            
            // Update JobSetting
            jobSettingService.updateParams("TRANSACTION_PROCESSING_STEP", params, "API");
            
            response.put("message", "PageSize updated successfully");
            response.put("pageSize", pageSize);
            response.put("note", "New pageSize will be applied on next job run");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to update pageSize: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Tạo hoặc cập nhật config đầy đủ cho Transaction Job
     * POST /api/transaction-job/config
     * Body: {"pageSize": 20, "description": "Transaction processing step"}
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> createOrUpdateConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object pageSizeObj = request.get("pageSize");
            if (pageSizeObj == null) {
                response.put("error", "pageSize is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            int pageSize;
            if (pageSizeObj instanceof Number) {
                pageSize = ((Number) pageSizeObj).intValue();
            } else if (pageSizeObj instanceof String) {
                pageSize = Integer.parseInt((String) pageSizeObj);
            } else {
                response.put("error", "pageSize must be a number");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (pageSize <= 0) {
                response.put("error", "pageSize must be greater than 0");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Tạo params JSON
            String params = String.format("{\"pageSize\": %d}", pageSize);
            String description = request.get("description") != null ? 
                request.get("description").toString() : "Transaction processing step";
            Integer status = request.get("status") != null && request.get("status") instanceof Number ?
                ((Number) request.get("status")).intValue() : 1;
            
            // Create or update JobSetting
            com.yourcompany.batch.service.dto.JobSettingDTO dto = 
                new com.yourcompany.batch.service.dto.JobSettingDTO();
            dto.setJobName("TRANSACTION_PROCESSING_STEP");
            dto.setDescription(description);
            dto.setStatus(status);
            dto.setParams(params);
            dto.setUpdatedBy("API");
            
            jobSettingService.saveOrUpdate(dto);
            
            response.put("message", "Config created/updated successfully");
            response.put("jobName", "TRANSACTION_PROCESSING_STEP");
            response.put("pageSize", pageSize);
            response.put("description", description);
            response.put("status", status);
            response.put("note", "New pageSize will be applied on next job run");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to create/update config: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

