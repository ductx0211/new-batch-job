# Troubleshooting: Ứng dụng tự động shutdown

## Vấn đề
Ứng dụng start thành công nhưng ngay sau đó tự động shutdown với log:
```
Started BatchJobApp in X seconds
[ionShutdownHook] Closing JPA EntityManagerFactory
[ionShutdownHook] HikariPool-1 - Shutdown initiated
Process finished with exit code 0
```

## Nguyên nhân có thể

### 1. Web Server không được khởi động
- **Triệu chứng**: Không thấy log "Tomcat started on port(s): 8084"
- **Nguyên nhân**: Web server không được start đúng cách
- **Giải pháp**: Đã thêm `ApplicationKeepAlive` component và set `web-application-type: servlet`

### 2. Main thread kết thúc trước khi web server start
- **Triệu chứng**: Ứng dụng start và shutdown ngay
- **Nguyên nhân**: Main thread không đợi web server
- **Giải pháp**: Đã extend `SpringBootServletInitializer` và set web application type

### 3. Chạy từ IDE (IntelliJ/Eclipse)
- **Triệu chứng**: Chạy từ IDE thì shutdown, chạy từ command line thì OK
- **Nguyên nhân**: IDE có thể có cấu hình riêng
- **Giải pháp**: 
  - Chạy từ command line: `java -jar target/new-batch-job-1.0.0.jar`
  - Hoặc cấu hình Run Configuration trong IDE để không auto-exit

### 4. Môi trường Windows
- **Triệu chứng**: Chạy trên Windows thì shutdown, trên Linux/Mac thì OK
- **Nguyên nhân**: Có thể liên quan đến cách xử lý shutdown hook trên Windows
- **Giải pháp**: Đã thêm `register-shutdown-hook: true` và `ApplicationKeepAlive`

## Các thay đổi đã thực hiện

### 1. BatchJobApp.java
- Extend `SpringBootServletInitializer`
- Set `web-application-type: SERVLET` trong code
- Set `registerShutdownHook(true)`
- Thêm logging để debug

### 2. ApplicationKeepAlive.java (mới)
- Component để đảm bảo ứng dụng không shutdown
- Listen `WebServerInitializedEvent` để log khi web server start
- Listen `ContextClosedEvent` để log khi context close

### 3. application.yml
- `web-application-type: servlet`
- `keep-alive: true`
- `register-shutdown-hook: true`

### 4. HealthController.java
- REST endpoints để giữ web server chạy
- `/api/health` và `/api/`

## Cách kiểm tra

### 1. Kiểm tra web server có start không
```bash
# Sau khi start, kiểm tra log có dòng:
# "Tomcat started on port(s): 8084 (http)"
```

### 2. Test endpoints
```bash
# Health check
curl http://localhost:8084/api/health

# Home
curl http://localhost:8084/api/

# Actuator
curl http://localhost:8084/actuator/health
```

### 3. Kiểm tra process
```bash
# Windows
netstat -ano | findstr :8084
tasklist | findstr java

# Linux/Mac
lsof -i :8084
ps aux | grep java
```

## Cách chạy đúng

### Từ command line (khuyến nghị)
```bash
# Build
mvn clean package -DskipTests

# Chạy
java -jar target/new-batch-job-1.0.0.jar
```

### Từ IDE
1. **IntelliJ IDEA**:
   - Run Configuration → Edit Configurations
   - Main class: `com.yourcompany.batch.BatchJobApp`
   - VM options: (để trống)
   - Program arguments: (để trống)
   - ✅ Không check "Allow parallel run"
   - ✅ Không check "Single instance only"

2. **Eclipse**:
   - Run → Run Configurations
   - Main class: `com.yourcompany.batch.BatchJobApp`
   - Arguments → VM arguments: (để trống)

## Nếu vẫn shutdown

### Thêm scheduled job mẫu
Nếu vẫn shutdown, có thể cần thêm một scheduled job mẫu:

```java
@Component
public class KeepAliveScheduler {
    
    @Scheduled(fixedDelay = 60000) // Mỗi 60 giây
    public void keepAlive() {
        // Job này chỉ để giữ ứng dụng chạy
    }
}
```

### Kiểm tra log level
Thêm vào `application.yml`:
```yaml
logging:
  level:
    org.springframework.boot: DEBUG
    org.springframework.web: DEBUG
```

Để xem chi tiết quá trình khởi động web server.

## Liên hệ
Nếu vẫn gặp vấn đề, vui lòng cung cấp:
1. Log đầy đủ từ khi start đến khi shutdown
2. Môi trường (OS, Java version, IDE)
3. Cách chạy (command line hay IDE)

