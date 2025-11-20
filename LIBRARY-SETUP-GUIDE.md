# Hướng dẫn sử dụng Library trong Project khác

## Vấn đề: "Not a managed type" hoặc "consider defining a bean"

Khi import library vào project khác, bạn cần cấu hình thêm để Spring Boot có thể scan entities và repositories.

## Giải pháp

### Cách 1: Thêm cấu hình vào Application class (Khuyến nghị)

Thêm các annotation sau vào class `@SpringBootApplication` của project sử dụng library:

```java
@SpringBootApplication
@EntityScan(basePackages = {
    "com.yourcompany.batch.domain",  // Package entities của library
    "com.yourproject.domain"          // Package entities của project bạn (nếu có)
})
@EnableJpaRepositories(basePackages = {
    "com.yourcompany.batch.repository",  // Package repositories của library
    "com.yourproject.repository"         // Package repositories của project bạn (nếu có)
})
@ComponentScan(basePackages = {
    "com.yourcompany.batch",  // Package components của library
    "com.yourproject"          // Package components của project bạn
})
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### Cách 2: Tạo Configuration class riêng

Tạo một configuration class trong project của bạn:

```java
package com.yourproject.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
    "com.yourcompany.batch.domain",
    "com.yourproject.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.yourcompany.batch.repository",
    "com.yourproject.repository"
})
@ComponentScan(basePackages = {
    "com.yourcompany.batch",
    "com.yourproject"
})
public class BatchJobLibraryConfiguration {
    // Configuration để enable library
}
```

### Cách 3: Cấu hình trong application.yml

Nếu bạn muốn cấu hình qua properties (Spring Boot 2.7+):

```yaml
spring:
  jpa:
    packages-to-scan:
      - com.yourcompany.batch.domain
      - com.yourproject.domain
```

**Lưu ý**: Cách này chỉ áp dụng cho entities, vẫn cần `@EnableJpaRepositories` cho repositories.

## Kiểm tra

Sau khi cấu hình, kiểm tra log khi start application:

```
Mapped "{[/api/jobs]}" onto ...
```

Nếu không thấy lỗi "Not a managed type" hoặc "consider defining a bean", nghĩa là đã cấu hình đúng.

## Troubleshooting

### Lỗi: "Not a managed type: com.yourcompany.batch.domain.JobSetting"

**Nguyên nhân**: Entity chưa được scan.

**Giải pháp**: Đảm bảo `@EntityScan(basePackages = "com.yourcompany.batch.domain")` đã được thêm vào Application class hoặc Configuration class.

### Lỗi: "consider defining a bean of type 'JobSettingRepository'"

**Nguyên nhân**: Repository chưa được scan.

**Giải pháp**: Đảm bảo `@EnableJpaRepositories(basePackages = "com.yourcompany.batch.repository")` đã được thêm vào Application class hoặc Configuration class.

### Lỗi: "No qualifying bean of type 'JobSettingService'"

**Nguyên nhân**: Component/Service chưa được scan.

**Giải pháp**: Đảm bảo `@ComponentScan(basePackages = "com.yourcompany.batch")` đã được thêm vào Application class hoặc Configuration class.

