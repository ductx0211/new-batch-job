package com.yourcompany.batch.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Component để đảm bảo ứng dụng không tự động shutdown
 * và log thông tin về web server
 */
@Component
public class ApplicationKeepAlive implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationKeepAlive.class);

    @PostConstruct
    public void init() {
        log.info("ApplicationKeepAlive initialized - Application will stay running");
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        log.info("Web server started successfully on port: {}", port);
        log.info("Application is ready to accept requests");
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        log.warn("Application context is being closed - this should only happen on shutdown");
    }
}

