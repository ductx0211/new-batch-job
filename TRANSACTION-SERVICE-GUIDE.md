# Hướng dẫn Transaction Service - Dynamic PageSize

## Tổng quan

Transaction Service sử dụng **JDBC trực tiếp** thay vì Repository để lấy data. PageSize có thể được **thay đổi động** mà không cần restart app thông qua JobSetting và REST API.

## Kiến trúc

### 1. TransactionService
- **File**: `src/main/java/com/yourcompany/batch/service/TransactionService.java`
- **Chức năng**: Lấy data từ database bằng JDBC
- **Methods**:
  - `findTransactionsWithLimitAndOffset(limit, offset)`: Lấy transaction với limit/offset
  - `countAllTransactions()`: Đếm tổng số transaction
  - `findTransactionsByBranch(branch, limit, offset)`: Lấy theo branch
  - `findTransactionsByDateRange(fromDate, toDate, limit, offset)`: Lấy theo khoảng thời gian

### 2. TransactionReader
- **File**: `src/main/java/com/yourcompany/batch/batch/reader/TransactionReader.java`
- **Chức năng**: Đọc transaction với pageSize động từ JobSetting
- **Tính năng**:
  - Đọc pageSize từ JobSetting mỗi lần job chạy
  - Không cần restart app để thay đổi pageSize
  - Default pageSize = 10 nếu không có config

### 3. JobSettingService
- **File**: `src/main/java/com/yourcompany/batch/service/JobSettingService.java`
- **Chức năng**: Quản lý JobSetting (config cho job)
- **Methods**:
  - `findByJobName(jobName)`: Lấy config theo job name
  - `saveOrUpdate(dto)`: Lưu hoặc cập nhật config
  - `updateParams(jobName, params, updatedBy)`: Cập nhật params

### 4. TransactionJobController
- **File**: `src/main/java/com/yourcompany/batch/controller/TransactionJobController.java`
- **Chức năng**: REST API để quản lý config
- **Endpoints**:
  - `GET /api/transaction-job/config`: Lấy config hiện tại
  - `PUT /api/transaction-job/config/page-size`: Cập nhật pageSize
  - `POST /api/transaction-job/config`: Tạo/cập nhật config đầy đủ

## Cách sử dụng

### 1. Lấy config hiện tại

```bash
curl -X GET http://localhost:8084/api/transaction-job/config
```

Response:
```json
{
  "jobName": "TRANSACTION_PROCESSING_STEP",
  "description": "Transaction processing step",
  "status": 1,
  "params": "{\"pageSize\": 10}",
  "message": "Config found"
}
```

### 2. Cập nhật pageSize

```bash
curl -X PUT http://localhost:8084/api/transaction-job/config/page-size \
  -H "Content-Type: application/json" \
  -d '{"pageSize": 20}'
```

Response:
```json
{
  "message": "PageSize updated successfully",
  "pageSize": 20,
  "note": "New pageSize will be applied on next job run"
}
```

### 3. Tạo/cập nhật config đầy đủ

```bash
curl -X POST http://localhost:8084/api/transaction-job/config \
  -H "Content-Type: application/json" \
  -d '{
    "pageSize": 25,
    "description": "Transaction processing step with pageSize 25",
    "status": 1
  }'
```

### 4. Chạy job

```bash
curl -X POST http://localhost:8084/api/jobs/sample/run
```

Job sẽ tự động đọc pageSize từ JobSetting và sử dụng giá trị mới.

## Luồng hoạt động

1. **Job Start**: `SampleJob` được trigger
2. **TransactionStepBuilder.reader()**: 
   - Gọi `transactionReader.initialize(getStepName())`
   - Reader load pageSize từ JobSetting (jobName = "TRANSACTION_PROCESSING_STEP")
3. **TransactionReader.read()**: 
   - Đọc pageSize từ config (mỗi lần bắt đầu page mới)
   - Gọi `transactionService.findTransactionsWithLimitAndOffset(pageSize, offset)`
4. **TransactionService**: 
   - Sử dụng JDBC để query database
   - Trả về danh sách transaction
5. **Processor & Writer**: Xử lý và ghi kết quả

## Thay đổi pageSize động

### Cách 1: Qua REST API (Khuyến nghị)

```bash
# Thay đổi pageSize từ 10 sang 20
curl -X PUT http://localhost:8084/api/transaction-job/config/page-size \
  -H "Content-Type: application/json" \
  -d '{"pageSize": 20}'

# Chạy job ngay sau đó
curl -X POST http://localhost:8084/api/jobs/sample/run
```

Job sẽ tự động sử dụng pageSize = 20 mà không cần restart app.

### Cách 2: Qua Database

```sql
-- Xem config hiện tại
SELECT * FROM batch_job_setting WHERE job_name = 'TRANSACTION_PROCESSING_STEP';

-- Cập nhật pageSize
UPDATE batch_job_setting 
SET params = '{"pageSize": 20}',
    updated_by = 'ADMIN',
    updated_date = CURRENT_TIMESTAMP
WHERE job_name = 'TRANSACTION_PROCESSING_STEP';
```

### Cách 3: Qua H2 Console

1. Mở H2 Console: http://localhost:8084/h2-console
2. JDBC URL: `jdbc:h2:mem:batchdb`
3. Username: `sa`
4. Query:
```sql
UPDATE batch_job_setting 
SET params = '{"pageSize": 20}'
WHERE job_name = 'TRANSACTION_PROCESSING_STEP';
```

## JobSetting Schema

Table: `batch_job_setting`

```sql
CREATE TABLE batch_job_setting (
    job_name VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255),
    status INTEGER,
    params VARCHAR(4000),  -- JSON format: {"pageSize": 10}
    created_by VARCHAR(50),
    created_date TIMESTAMP,
    updated_by VARCHAR(50),
    updated_date TIMESTAMP
);
```

## Ví dụ

### Ví dụ 1: Thay đổi pageSize từ 10 sang 50

```bash
# 1. Cập nhật pageSize
curl -X PUT http://localhost:8084/api/transaction-job/config/page-size \
  -H "Content-Type: application/json" \
  -d '{"pageSize": 50}'

# 2. Chạy job
curl -X POST http://localhost:8084/api/jobs/sample/run

# 3. Kiểm tra log
# Job sẽ đọc 50 transactions mỗi lần thay vì 10
```

### Ví dụ 2: Kiểm tra config

```bash
# Lấy config hiện tại
curl -X GET http://localhost:8084/api/transaction-job/config

# Response
{
  "jobName": "TRANSACTION_PROCESSING_STEP",
  "params": "{\"pageSize\": 50}",
  "message": "Config found"
}
```

## Lưu ý

1. **PageSize động**: PageSize được đọc từ JobSetting mỗi lần job chạy, không cần restart app
2. **Default pageSize**: Nếu không có config, sử dụng default = 10
3. **Validation**: PageSize phải > 0, nếu không sẽ sử dụng default
4. **JobSetting**: JobSetting được lưu trong database, persist qua các lần restart
5. **JDBC vs Repository**: TransactionService sử dụng JDBC trực tiếp, không phụ thuộc vào JPA Repository

## Troubleshooting

### PageSize không thay đổi

1. **Kiểm tra JobSetting**:
```sql
SELECT * FROM batch_job_setting WHERE job_name = 'TRANSACTION_PROCESSING_STEP';
```

2. **Kiểm tra params format**:
```json
{"pageSize": 20}
```

3. **Kiểm tra log**: Job sẽ log pageSize khi khởi tạo:
```
TransactionReader initialized with pageSize: 20 for job: TRANSACTION_PROCESSING_STEP
```

### Lỗi khi query database

1. **Kiểm tra table transaction có tồn tại không**
2. **Kiểm tra database connection**
3. **Kiểm tra SQL syntax** (có thể khác nhau giữa H2 và Oracle)

### Config không được lưu

1. **Kiểm tra transaction**: Đảm bảo transaction được commit
2. **Kiểm tra database connection**
3. **Kiểm tra JobSetting table có tồn tại không**

## So sánh với Repository

| Tính năng | Repository | Service (JDBC) |
|-----------|-----------|----------------|
| **Cách lấy data** | JPA Repository | JDBC trực tiếp |
| **Performance** | Có overhead của JPA | Nhanh hơn, ít overhead |
| **Flexibility** | Hạn chế bởi JPA | Linh hoạt hơn, có thể tùy chỉnh SQL |
| **Maintenance** | Dễ maintain, type-safe | Cần viết SQL thủ công |
| **Dynamic config** | Cần restart app | Có thể thay đổi động |

## Kết luận

TransactionService sử dụng JDBC trực tiếp để lấy data, cho phép:
- **Thay đổi pageSize động** mà không cần restart app
- **Performance tốt hơn** (ít overhead)
- **Linh hoạt hơn** trong việc tùy chỉnh SQL
- **Dễ dàng quản lý** qua REST API

