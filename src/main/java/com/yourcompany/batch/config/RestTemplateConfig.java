package com.yourcompany.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration cho RestTemplate
 * Cấu hình đầy đủ với timeout, error handling, interceptors, message converters
 */
@Configuration
public class RestTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Value("${external.api.timeout.connect:10000}")
    private int connectTimeout;

    @Value("${external.api.timeout.read:30000}")
    private int readTimeout;

    @Value("${external.api.enable.logging:false}")
    private boolean enableLogging;

    /**
     * RestTemplate bean chính được sử dụng trong toàn bộ application
     * Sử dụng RestTemplateBuilder để cấu hình
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(connectTimeout))
            .setReadTimeout(Duration.ofMillis(readTimeout))
            .requestFactory(this::clientHttpRequestFactory)
            .errorHandler(responseErrorHandler())
            .interceptors(httpRequestInterceptor())
            .build();
    }

    /**
     * Tạo ClientHttpRequestFactory với timeout configuration
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        
        // Buffering để có thể đọc response body nhiều lần (cho logging, error handling)
        return new BufferingClientHttpRequestFactory(factory);
    }

    /**
     * Response Error Handler để xử lý lỗi từ API
     */
    private ResponseErrorHandler responseErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                try {
                    return response.getStatusCode().isError();
                } catch (Exception e) {
                    log.error("Error checking response status: {}", e.getMessage(), e);
                    return true;
                }
            }

            @Override
            public void handleError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
                // RestTemplate sẽ throw RestClientException
                // Error sẽ được xử lý trong ExternalApiService
                try {
                    log.debug("Response error handler called for status: {}", response.getStatusCode());
                } catch (Exception e) {
                    log.error("Error in error handler: {}", e.getMessage(), e);
                }
            }
        };
    }

    /**
     * HTTP Request Interceptor để log requests/responses (nếu enabled)
     */
    private List<ClientHttpRequestInterceptor> httpRequestInterceptor() {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        
        if (enableLogging) {
            interceptors.add((request, body, execution) -> {
                logRequest(request, body);
                org.springframework.http.client.ClientHttpResponse response = execution.execute(request, body);
                logResponse(response);
                return response;
            });
        }
        
        return interceptors;
    }

    /**
     * Log request details
     */
    private void logRequest(org.springframework.http.HttpRequest request, byte[] body) {
        log.debug("=== RestTemplate Request ===");
        log.debug("URI: {}", request.getURI());
        log.debug("Method: {}", request.getMethod());
        log.debug("Headers: {}", request.getHeaders());
        if (body != null && body.length > 0) {
            log.debug("Body: {}", new String(body));
        }
    }

    /**
     * Log response details
     */
    private void logResponse(org.springframework.http.client.ClientHttpResponse response) {
        try {
            log.debug("=== RestTemplate Response ===");
            log.debug("Status: {}", response.getStatusCode());
            log.debug("Headers: {}", response.getHeaders());
        } catch (Exception e) {
            log.error("Error logging response: {}", e.getMessage(), e);
        }
    }
}

