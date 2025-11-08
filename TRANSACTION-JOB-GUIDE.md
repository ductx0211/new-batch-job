# Hướng dẫn Transaction Job

## Tổng quan

Transaction Job được thiết kế để xử lý các transaction từ database. Job này đọc **10 transactions mỗi lần** từ database và xử lý chúng.

## Cấu trúc

### 1. Transaction Entity
- **File**: `src/main/java/com/yourcompany/batch/domain/Transaction.java`
- **Fields**:
  - `id`: Long (Primary Key, Auto Increment)
  - `branch`: String (100 chars) - Chi nhánh
  - `name`: String (255 chars) - Tên transaction
  - `amount`: BigDecimal (19,2) - Số tiền
  - `createDate`: Instant - Ngày tạo

### 2. TransactionRepository
- **File**: `src/main/java/com/yourcompany/batch/repository/TransactionRepository.java`
- **Methods**:
  - `findAllOrderByCreateDate(Pageable)`: Lấy tất cả transaction với phân trang, sắp xếp theo create_date
  - `countAll()`: Đếm tổng số transaction
  - `findTransactionsWithOffset(int limit, int offset)`: Lấy transaction với offset/limit
  - `findUnprocessedTransactions(Instant, Pageable)`: Lấy transaction chưa xử lý

### 3. TransactionReader
- **File**: `src/main/java/com/yourcompany/batch/batch/reader/TransactionReader.java`
- **Chức năng**: Đọc 10 transactions mỗi lần từ database
- **Cách hoạt động**:
  - Sử dụng pagination với Pageable
  - Mỗi lần đọc 10 rows (PAGE_SIZE = 10)
  - Tự động load trang tiếp theo khi hết data

### 4. TransactionStepBuilder
- **File**: `src/main/java/com/yourcompany/batch/sample/TransactionStepBuilder.java`
- **Chức năng**: Step Builder để xử lý Transaction
- **Flow**:
  1. **Reader**: Đọc 10 transactions từ database
  2. **Processor**: Xử lý từng transaction (validate, tính toán, etc.)
  3. **Writer**: Ghi kết quả xử lý (update database, log, etc.)

## Cách sử dụng

### 1. Tạo Transaction Table

Table sẽ được tự động tạo bởi Hibernate khi `spring.jpa.hibernate.ddl-auto=create-drop` hoặc `update`.

Hoặc chạy SQL script:
```sql
CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch VARCHAR(100),
    name VARCHAR(255),
    amount DECIMAL(19, 2),
    create_date TIMESTAMP
);
```

### 2. Insert Sample Data

#### Cách 1: Sử dụng Data Initializer (Khuyến nghị)
- Set property `batch.init.sample-data=true` trong `application.yml`
- Data initializer sẽ tự động tạo 25 sample transactions khi ứng dụng start

#### Cách 2: Insert thủ công
```sql
INSERT INTO transaction (branch, name, amount, create_date) VALUES
('HN001', 'Transaction 1', 1000.50, CURRENT_TIMESTAMP),
('HN001', 'Transaction 2', 2000.75, CURRENT_TIMESTAMP),
('HN002', 'Transaction 3', 3000.25, CURRENT_TIMESTAMP),
-- ...
```

### 3. Chạy Job

#### Cách 1: Chạy qua REST API
```bash
curl -X POST http://localhost:8084/api/jobs/sample/run
```

#### Cách 2: Chạy thủ công trong code
```java
@Autowired
private SampleJob sampleJob;

sampleJob.runManual();
```

#### Cách 3: Scheduled Job
Uncomment `@Scheduled` annotation trong `SampleJob.java`:
```java
@Scheduled(cron = "0 */5 * * * ?") // Mỗi 5 phút
@SchedulerLock(
    name = "SAMPLE_JOB_SCHEDULER_LOCK",
    lockAtMostFor = "10m",
    lockAtLeastFor = "5m"
)
public void schedule() {
    runBySchedule();
}
```

## Luồng xử lý

1. **Job Start**: `SampleJob` được trigger
2. **TransactionStepBuilder**: 
   - Đọc 10 transactions đầu tiên từ database
   - Xử lý từng transaction (processor)
   - Ghi kết quả (writer)
3. **Lặp lại**: Đọc 10 transactions tiếp theo cho đến khi hết data
4. **Job Complete**: Ghi log và kết quả

## Ví dụ log

```
INFO  - TransactionReader initialized for reading transactions
INFO  - Total transactions to process: 25
INFO  - Loaded page 0: 10 transactions
INFO  - Processing transaction: id=1, branch=HN001, name=Transaction 1, amount=1000.50
INFO  - Writing processed transaction: id=1, branch=HN001, name=Transaction 1, amount=1000.50
INFO  - Written 10 transactions
INFO  - Loaded page 1: 10 transactions
...
INFO  - Processed 25 transactions
```

## Tùy chỉnh

### Thay đổi số lượng rows mỗi lần đọc

Trong `TransactionReader.java`:
```java
private static final int PAGE_SIZE = 10; // Thay đổi thành số lượng mong muốn
```

### Thêm logic xử lý trong Processor

Trong `TransactionStepBuilder.processor()`:
```java
protected ItemProcessor<Transaction, Transaction> processor() {
    return transaction -> {
        // Thêm logic xử lý ở đây
        if (transaction.getAmount().compareTo(new BigDecimal(10000)) > 0) {
            // Xử lý transaction lớn
            transaction.setName(transaction.getName() + " [LARGE]");
        }
        return transaction;
    };
}
```

### Thêm logic ghi trong Writer

Trong `TransactionStepBuilder.writer()`:
```java
protected ItemWriter<Transaction> writer() {
    return transactions -> {
        for (Transaction transaction : transactions) {
            // Update transaction status
            // transaction.setProcessed(true);
            // transactionRepository.save(transaction);
            
            // Hoặc gọi API, ghi file, etc.
        }
    };
}
```

## Kiểm tra kết quả

### Xem trong H2 Console
1. Mở browser: http://localhost:8084/h2-console
2. JDBC URL: `jdbc:h2:mem:batchdb`
3. Username: `sa`
4. Password: (để trống)
5. Query: `SELECT * FROM transaction;`

### Xem Job Log
- Table: `job_log`
- Query: `SELECT * FROM job_log WHERE job_name = 'TRANSACTION_PROCESSING_STEP' ORDER BY created_date DESC;`

### Xem Job Result
- Table: `job_log_result`
- Query: `SELECT * FROM job_log_result WHERE job_name = 'TRANSACTION_PROCESSING_STEP' ORDER BY created_date DESC;`

## Troubleshooting

### Không có data để xử lý
- Kiểm tra table `transaction` có data không
- Kiểm tra `batch.init.sample-data=true` trong `application.yml`
- Chạy lại data initializer hoặc insert data thủ công

### Reader không đọc được data
- Kiểm tra database connection
- Kiểm tra transaction table đã được tạo chưa
- Kiểm tra log để xem có lỗi gì không

### Processor/Writer gặp lỗi
- Kiểm tra logic xử lý trong processor/writer
- Kiểm tra exception trong log
- Job sẽ skip các item bị lỗi và tiếp tục xử lý

## Lưu ý

1. **Pagination**: Reader sử dụng pagination, mỗi lần đọc 10 rows
2. **Chunk Size**: Default chunk size là 1000, có thể thay đổi trong `AbstractStepBuilder`
3. **Transaction**: Mỗi chunk được xử lý trong một transaction
4. **Error Handling**: Job sẽ skip các item bị lỗi và tiếp tục xử lý
5. **Performance**: Với số lượng lớn, nên tối ưu query và index

