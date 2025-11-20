package com.yourcompany.batch.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration class để enable component scan khi library được import vào project khác.
 * 
 * Class này sẽ tự động được load khi Spring Boot khởi động nếu library được import.
 * Đảm bảo:
 * - Các component, service, configuration trong package com.yourcompany.batch được scan
 * - Entities và repositories được scan thông qua các configuration class riêng
 * 
 * Lưu ý: 
 * - Chỉ scan package com.yourcompany.batch để tránh xung đột với các component của project sử dụng library
 * - EntityScan và JpaRepositories được tách riêng để đảm bảo thứ tự load đúng
 * - Load sau HibernateJpaAutoConfiguration để đảm bảo JPA đã được cấu hình
 */
@AutoConfiguration(after = {HibernateJpaAutoConfiguration.class, BatchJobJpaRepositoriesConfiguration.class})
@Import({
    BatchJobEntityScanConfiguration.class,
    BatchJobJpaRepositoriesConfiguration.class
})
@ComponentScan(basePackages = "com.yourcompany.batch")
public class BatchJobAutoConfiguration {
    // Auto-configuration class - không cần code bên trong
    // Spring Boot sẽ tự động scan và load các repository, entity và component
    // EntityScan và JpaRepositories được import từ các configuration class riêng
}

