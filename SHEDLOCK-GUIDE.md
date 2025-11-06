# Hướng dẫn ShedLock - Distributed Locking

## Tổng quan

ShedLock được sử dụng để đảm bảo chỉ có **1 instance** chạy scheduled job tại một thời điểm, ngay cả khi có nhiều instance của ứng dụng đang chạy.

## Cấu hình

### 1. ShedLockConfiguration.java
- **@EnableSchedulerLock**: Enable ShedLock cho Spring
- **defaultLockAtMostFor**: Lock tối đa 10 phút (nếu job crash, lock sẽ tự động release)
- **defaultLockAtLeastFor**: Lock tối thiểu 5 phút (tránh chạy quá thường xuyên)
- **LockProvider**: Sử dụng JdbcTemplateLockProvider với database

### 2. Database Table
ShedLock sẽ tự động tạo table `shedlock` khi ứng dụng start:
- **name**: Tên của lock (unique)
- **lock_until**: Thời gian lock hết hạn
- **locked_at**: Thời gian lock được tạo
- **locked_by**: Instance nào đang giữ lock

## Cách sử dụng

### Trong Job Class

```java
@Component
public class MyJob extends AbstractJob {
    
    @Scheduled(cron = "0 */5 * * * ?") // Chạy mỗi 5 phút
    @SchedulerLock(
        name = "MY_JOB_SCHEDULER_LOCK",
        lockAtMostFor = "10m",  // Lock tối đa 10 phút
        lockAtLeastFor = "5m"   // Lock tối thiểu 5 phút
    )
    public void schedule() {
        runBySchedule();
    }
}
```

### Tham số @SchedulerLock

- **name**: Tên của lock (phải unique cho mỗi job)
- **lockAtMostFor**: Thời gian lock tối đa (nếu job crash, lock sẽ tự động release)
- **lockAtLeastFor**: Thời gian lock tối thiểu (tránh chạy quá thường xuyên)

## Ví dụ: SampleJob

```java
@SchedulerLock(
    name = "SAMPLE_JOB_SCHEDULER_LOCK",
    lockAtMostFor = "10m",
    lockAtLeastFor = "5m"
)
public void schedule() {
    runBySchedule();
}
```

## Cách hoạt động

1. **Instance 1** chạy job → Tạo lock trong database
2. **Instance 2** cố gắng chạy job → Kiểm tra lock → Thấy lock đang active → **Skip**
3. **Instance 1** hoàn thành job → Release lock
4. Lần chạy tiếp theo, instance nào check lock trước sẽ chạy

## Kiểm tra Lock

### Xem trong database (H2 Console)
```sql
SELECT * FROM shedlock;
```

### Xem qua logs
Khi job được skip do lock:
```
Lock already held by another instance
```

## Lưu ý

1. **Lock name phải unique**: Mỗi job phải có lock name riêng
2. **lockAtMostFor**: Nên set lớn hơn thời gian job chạy dự kiến
3. **lockAtLeastFor**: Nên set để tránh chạy quá thường xuyên
4. **Database**: ShedLock cần database để lưu lock, đảm bảo tất cả instance dùng chung database

## Troubleshooting

### Job không chạy
- Kiểm tra lock trong database: `SELECT * FROM shedlock;`
- Kiểm tra lock_until có hết hạn chưa
- Xóa lock thủ công nếu cần: `DELETE FROM shedlock WHERE name = 'LOCK_NAME';`

### Job chạy trùng lặp
- Kiểm tra tất cả instance có dùng chung database không
- Kiểm tra @SchedulerLock đã được thêm vào method chưa
- Kiểm tra ShedLockConfiguration đã được load chưa

