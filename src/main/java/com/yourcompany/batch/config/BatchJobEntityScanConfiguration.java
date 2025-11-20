package com.yourcompany.batch.config;

import jakarta.persistence.Entity;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Configuration riêng để scan entities
 * Tách riêng để đảm bảo entities được scan trước khi repositories được tạo
 * Sử dụng @AutoConfigureAfter để đảm bảo load sau HibernateJpaAutoConfiguration
 * 
 * Lưu ý: Nếu project sử dụng library đã có @EntityScan, cần merge packages:
 * @EntityScan(basePackages = {"com.yourcompany.batch.domain", "com.yourproject.domain"})
 */
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass(Entity.class)
@EntityScan(basePackages = "com.yourcompany.batch.domain")
public class BatchJobEntityScanConfiguration {
    // Configuration để scan entities trong package com.yourcompany.batch.domain
    // Nếu project đã có @EntityScan, cần thêm package này vào basePackages của project
}

