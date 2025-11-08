package com.yourcompany.batch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service để gọi API bên ngoài
 */
@Service
public class ExternalApiService {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${external.api.url:http://localhost:8080/api/transaction}")
    private String externalApiUrl;

    @Value("${external.api.timeout:30000}")
    private int timeout;

    /**
     * Gọi API bên ngoài để xử lý transaction
     * 
     * @param transactionId ID của transaction
     * @param branch Branch của transaction
     * @return true nếu call API thành công, false nếu thất bại
     */
    public boolean processTransaction(Long transactionId, String branch) {
        try {
            log.info("Calling external API for transaction id={}, branch={}", transactionId, branch);

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", transactionId);
            requestBody.put("branch", branch);

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Có thể thêm authentication header nếu cần
            // headers.set("Authorization", "Bearer " + token);

            // Tạo HttpEntity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Gọi API
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                externalApiUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            // Kiểm tra response
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully called external API for transaction id={}, branch={}, response={}", 
                    transactionId, branch, response.getBody());
                return true;
            } else {
                log.warn("External API returned non-success status for transaction id={}, branch={}, status={}", 
                    transactionId, branch, response.getStatusCode());
                return false;
            }

        } catch (RestClientException e) {
            log.error("Error calling external API for transaction id={}, branch={}: {}", 
                transactionId, branch, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error calling external API for transaction id={}, branch={}: {}", 
                transactionId, branch, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gọi API bên ngoài với custom request body
     * 
     * @param requestBody Request body tùy chỉnh
     * @return Response từ API hoặc null nếu lỗi
     */
    public Map<String, Object> callExternalApi(Map<String, Object> requestBody) {
        try {
            log.debug("Calling external API with request body: {}", requestBody);

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Có thể thêm authentication header nếu cần
            // headers.set("Authorization", "Bearer " + token);

            // Tạo HttpEntity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Gọi API
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                externalApiUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            // Kiểm tra response
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Successfully called external API, response: {}", response.getBody());
                return response.getBody();
            } else {
                log.warn("External API returned non-success status: {}", response.getStatusCode());
                return null;
            }

        } catch (RestClientException e) {
            log.error("Error calling external API: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error calling external API: {}", e.getMessage(), e);
            return null;
        }
    }
}

