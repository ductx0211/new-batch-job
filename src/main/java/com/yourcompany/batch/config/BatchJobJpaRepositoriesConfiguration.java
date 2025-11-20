package com.yourcompany.batch.config;

import jakarta.persistence.Entity;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.Repository;

/**
 * Configuration riêng để enable JPA repositories
 * Đảm bảo entities đã được scan trước khi repositories được tạo
 * Sử dụng @AutoConfigureAfter để đảm bảo load sau EntityScan và HibernateJpaAutoConfiguration
 * 
 * Lưu ý: Nếu project sử dụng library đã có @EnableJpaRepositories, cần merge packages:
 * @EnableJpaRepositories(basePackages = {"com.yourcompany.batch.repository", "com.yourproject.repository"})
 */
@AutoConfiguration(after = {HibernateJpaAutoConfiguration.class, BatchJobEntityScanConfiguration.class})
@ConditionalOnClass({Entity.class, Repository.class})
@EnableJpaRepositories(basePackages = "com.yourcompany.batch.repository")
public class BatchJobJpaRepositoriesConfiguration {
    // Configuration để enable JPA repositories trong package com.yourcompany.batch.repository
    // Nếu project đã có @EnableJpaRepositories, cần thêm package này vào basePackages của project
}

