# Hướng dẫn sử dụng Sample Job

## Tổng quan

Đã tạo một job mẫu (`SampleJob`) để demo cách sử dụng framework. Job này bao gồm:

1. **SampleJob** - Job chính
2. **SampleStepBuilder** - Step với Reader/Processor/Writer
3. **SampleTasklet** - Tasklet đơn giản

## Cấu trúc Sample Job

### SampleJob
- **Job Name**: `SAMPLE_JOB`
- **Steps**: 
  1. `SampleStepBuilder` - Xử lý danh sách số (1-10), nhân mỗi số với 2
  2. `SampleTasklet` - Thực hiện tasklet đơn giản

### SampleStepBuilder
- **Step Name**: `SAMPLE_STEP`
- **Reader**: Đọc danh sách số từ 1 đến 10
- **Processor**: Nhân mỗi số với 2
- **Writer**: Ghi log kết quả

### SampleTasklet
- **Tasklet Name**: `SAMPLE_TASKLET`
- **Logic**: Log thông tin và simulate work

## Cách sử dụng

### 1. Chạy Job qua REST API

```bash
# Trigger job
curl -X POST http://localhost:8084/api/jobs/sample/run

# Kiểm tra status
curl http://localhost:8084/api/jobs/sample/status
```

### 2. Chạy Job trong code

```java
@Autowired
private SampleJob sampleJob;

// Chạy job
sampleJob.runManual();
```

### 3. Chạy Job theo schedule (nếu enable)

Uncomment annotation `@Scheduled` trong `SampleJob.java`:

```java
@Scheduled(fixedDelay = 300000) // Chạy mỗi 5 phút
public void schedule() {
    runBySchedule();
}
```

## Kiểm tra kết quả

### 1. Xem logs
Job sẽ log các thông tin:
- Start/End của job và steps
- Số lượng items đã xử lý
- Kết quả xử lý

### 2. Xem trong database (H2 Console)
- Truy cập: http://localhost:8084/h2-console
- JDBC URL: `jdbc:h2:mem:batchdb`
- Username: `sa`
- Password: (để trống)

Xem các bảng:
- `JOB_LOG` - Logs của job
- `JOB_LOG_RESULT` - Kết quả của job
- `JOB_SETTING` - Settings của job

### 3. Xem qua Actuator
```bash
curl http://localhost:8084/actuator/health
```

## Tùy chỉnh Sample Job

### Thay đổi số lượng items
Trong `SampleStepBuilder.java`:
```java
@Override
protected int countTotalItems() {
    return 20; // Thay đổi từ 10 sang 20
}
```

### Thay đổi logic xử lý
Trong `SampleStepBuilder.java`:
```java
@Override
protected ItemProcessor<Integer, Integer> processor() {
    return item -> {
        // Thay đổi logic xử lý
        return item * 3; // Thay vì nhân 2
    };
}
```

### Thay đổi logic tasklet
Trong `SampleTasklet.java`:
```java
@Override
public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    // Thêm logic của bạn
    return RepeatStatus.FINISHED;
}
```

## Lưu ý

- Sample Job chỉ để demo, có thể xóa hoặc thay đổi theo nhu cầu
- Khi tạo job thực tế, nên tạo trong package riêng (không phải `sample`)
- Đảm bảo database đã được khởi tạo trước khi chạy job

