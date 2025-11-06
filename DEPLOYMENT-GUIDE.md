# Hướng dẫn Deploy và Chạy Ứng dụng

## Các vấn đề đã được sửa

### 1. Warning về OracleDialect
- **Vấn đề**: Hibernate cảnh báo không cần chỉ định rõ `database-platform`
- **Giải pháp**: Đã xóa `database-platform` trong `application.yml`, Hibernate sẽ tự động detect Oracle dialect

### 2. Warning về SimpleJobLauncher - No TaskExecutor
- **Vấn đề**: Spring Boot tự tạo SimpleJobLauncher nhưng không có TaskExecutor, dùng synchronous executor
- **Giải pháp**: Đã thêm bean `TaskExecutor` trong `BatchConfiguration.java` để JobLauncher tự động inject

### 3. Ứng dụng shutdown ngay sau khi start
- **Nguyên nhân**: Ứng dụng là Spring Boot application, nếu không có web server hoặc scheduled jobs, nó sẽ shutdown ngay
- **Giải pháp**: Ứng dụng cần có scheduled jobs hoặc web endpoints để giữ nó chạy

## Các bước khi copy sang máy khác

### 1. Kiểm tra Java version
```bash
java -version
# Phải là Java 21
```

### 2. Cấu hình JAVA_HOME (nếu cần)
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21

# Linux/Mac
export JAVA_HOME=/path/to/jdk-21
```

### 3. Cập nhật cấu hình database trong `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@YOUR_HOST:1521:YOUR_SID
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
```

### 4. Build và chạy
```bash
# Build
mvn clean package -DskipTests

# Chạy
java -jar target/new-batch-job-1.0.0.jar
```

## Các lưu ý quan trọng

1. **Database connection**: Đảm bảo Oracle database đang chạy và có thể kết nối được
2. **Port**: Mặc định chạy trên port 8084, có thể thay đổi trong `application.yml`
3. **Logs**: Kiểm tra logs để xem có lỗi gì không
4. **Dependencies**: Đảm bảo tất cả dependencies đã được download (Maven sẽ tự động download)

## Troubleshooting

### Lỗi: "No active profile set"
- **Giải pháp**: Đây chỉ là thông tin, không phải lỗi. Spring Boot sẽ dùng profile "default"

### Lỗi: "Bean jobRepository already defined"
- **Giải pháp**: Đây là thông tin bình thường, Spring Boot detect bean đã được định nghĩa và skip auto-configuration

### Lỗi: "No TaskExecutor has been set"
- **Giải pháp**: Đã được sửa bằng cách thêm bean TaskExecutor trong BatchConfiguration

### Ứng dụng shutdown ngay sau khi start
- **Nguyên nhân**: Không có scheduled jobs hoặc web endpoints
- **Giải pháp**: 
  - Tạo scheduled jobs trong code
  - Hoặc thêm `spring.main.web-application-type=servlet` để giữ ứng dụng chạy
  - Hoặc chạy với `--spring.main.web-application-type=none` nếu chỉ cần batch jobs

## Kiểm tra ứng dụng đang chạy

```bash
# Kiểm tra health endpoint
curl http://localhost:8084/actuator/health

# Kiểm tra process
# Windows
netstat -ano | findstr :8084

# Linux/Mac
lsof -i :8084
```

