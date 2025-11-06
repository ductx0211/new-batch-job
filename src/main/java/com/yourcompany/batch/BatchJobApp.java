package com.yourcompany.batch;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.yourcompany.batch.config.ApplicationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableScheduling
public class BatchJobApp extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(BatchJobApp.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BatchJobApp.class);
        // Đảm bảo ứng dụng không tự động exit
        app.setRegisterShutdownHook(true);
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.SERVLET);
        app.run(args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        log.info("Set default timezone to Asia/Ho_Chi_Minh");
        log.info("Application started - Web server should be running on configured port");
    }
}

