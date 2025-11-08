# Hướng dẫn Oracle Package - TRANSACTION_PKG

## Tổng quan

Oracle Package `TRANSACTION_PKG` được tạo để lấy dữ liệu transaction từ bảng `transaction` một cách hiệu quả. Package này cung cấp nhiều cách để lấy data:
- **Procedure với REF CURSOR**: Trả về kết quả qua OUT parameter
- **Pipelined Function**: Trả về table function có thể query trực tiếp
- **Function**: Trả về giá trị đơn

## Cấu trúc Package

### Package Specification
- **File**: `src/main/resources/db/oracle/TRANSACTION_PKG.sql`
- **Package Name**: `TRANSACTION_PKG`

### Procedures và Functions

#### 1. `get_transactions_10` (Procedure)
Lấy 10 rows transaction đầu tiên.

```sql
PROCEDURE get_transactions_10(
    p_cursor OUT SYS_REFCURSOR
);
```

**Cách sử dụng**:
```sql
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id NUMBER;
    v_branch VARCHAR2(100);
    v_name VARCHAR2(255);
    v_amount NUMBER(19, 2);
    v_create_date TIMESTAMP;
BEGIN
    TRANSACTION_PKG.get_transactions_10(v_cursor);
    
    LOOP
        FETCH v_cursor INTO v_id, v_branch, v_name, v_amount, v_create_date;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('ID: ' || v_id || ', Branch: ' || v_branch);
    END LOOP;
    
    CLOSE v_cursor;
END;
/
```

#### 2. `get_transactions` (Procedure)
Lấy N rows transaction với offset.

```sql
PROCEDURE get_transactions(
    p_limit IN NUMBER,
    p_offset IN NUMBER,
    p_cursor OUT SYS_REFCURSOR
);
```

**Cách sử dụng**:
```sql
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id NUMBER;
    v_branch VARCHAR2(100);
    v_name VARCHAR2(255);
    v_amount NUMBER(19, 2);
    v_create_date TIMESTAMP;
BEGIN
    TRANSACTION_PKG.get_transactions(10, 0, v_cursor);
    
    LOOP
        FETCH v_cursor INTO v_id, v_branch, v_name, v_amount, v_create_date;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('ID: ' || v_id || ', Branch: ' || v_branch);
    END LOOP;
    
    CLOSE v_cursor;
END;
/
```

#### 3. `get_transactions_10_piped` (Pipelined Function)
Lấy 10 rows transaction đầu tiên sử dụng pipelined function.

```sql
FUNCTION get_transactions_10_piped
RETURN transaction_tab PIPELINED;
```

**Cách sử dụng**:
```sql
SELECT * FROM TABLE(TRANSACTION_PKG.get_transactions_10_piped);
```

#### 4. `get_transactions_piped` (Pipelined Function)
Lấy N rows transaction với offset sử dụng pipelined function.

```sql
FUNCTION get_transactions_piped(
    p_limit IN NUMBER,
    p_offset IN NUMBER
)
RETURN transaction_tab PIPELINED;
```

**Cách sử dụng**:
```sql
SELECT * FROM TABLE(TRANSACTION_PKG.get_transactions_piped(10, 0));
```

#### 5. `count_transactions` (Function)
Đếm tổng số transaction.

```sql
FUNCTION count_transactions
RETURN NUMBER;
```

**Cách sử dụng**:
```sql
SELECT TRANSACTION_PKG.count_transactions() AS total_count FROM DUAL;
```

## Cài đặt Package

### 1. Chạy SQL Script

```bash
# Kết nối vào Oracle database
sqlplus username/password@database

# Chạy script
@src/main/resources/db/oracle/TRANSACTION_PKG.sql
```

Hoặc copy nội dung file `TRANSACTION_PKG.sql` và chạy trong SQL Developer, Toad, hoặc tool Oracle khác.

### 2. Kiểm tra Package đã được tạo

```sql
-- Kiểm tra package spec
SELECT * FROM user_source 
WHERE name = 'TRANSACTION_PKG' 
AND type = 'PACKAGE'
ORDER BY line;

-- Kiểm tra package body
SELECT * FROM user_source 
WHERE name = 'TRANSACTION_PKG' 
AND type = 'PACKAGE BODY'
ORDER BY line;
```

### 3. Test Package

```sql
-- Test 1: Lấy 10 rows đầu tiên
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id NUMBER;
    v_branch VARCHAR2(100);
    v_name VARCHAR2(255);
    v_amount NUMBER(19, 2);
    v_create_date TIMESTAMP;
BEGIN
    TRANSACTION_PKG.get_transactions_10(v_cursor);
    
    LOOP
        FETCH v_cursor INTO v_id, v_branch, v_name, v_amount, v_create_date;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('ID: ' || v_id || ', Branch: ' || v_branch || ', Name: ' || v_name || ', Amount: ' || v_amount);
    END LOOP;
    
    CLOSE v_cursor;
END;
/

-- Test 2: Lấy N rows với offset
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id NUMBER;
    v_branch VARCHAR2(100);
    v_name VARCHAR2(255);
    v_amount NUMBER(19, 2);
    v_create_date TIMESTAMP;
BEGIN
    TRANSACTION_PKG.get_transactions(10, 0, v_cursor);
    
    LOOP
        FETCH v_cursor INTO v_id, v_branch, v_name, v_amount, v_create_date;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('ID: ' || v_id || ', Branch: ' || v_branch);
    END LOOP;
    
    CLOSE v_cursor;
END;
/

-- Test 3: Sử dụng pipelined function
SELECT * FROM TABLE(TRANSACTION_PKG.get_transactions_10_piped);

-- Test 4: Đếm tổng số transaction
SELECT TRANSACTION_PKG.count_transactions() AS total_count FROM DUAL;
```

## Sử dụng trong Java

### 1. OracleTransactionService

Service để gọi Oracle Package từ Java code.

**File**: `src/main/java/com/yourcompany/batch/service/OracleTransactionService.java`

**Methods**:
- `getTransactions10()`: Lấy 10 rows đầu tiên
- `getTransactions(limit, offset)`: Lấy N rows với offset
- `getTransactions10Piped()`: Lấy 10 rows đầu tiên (pipelined)
- `getTransactionsPiped(limit, offset)`: Lấy N rows với offset (pipelined)
- `countTransactions()`: Đếm tổng số transaction

### 2. OracleTransactionReader

Reader để đọc transaction từ Oracle Package trong Spring Batch.

**File**: `src/main/java/com/yourcompany/batch/batch/reader/OracleTransactionReader.java`

**Tính năng**:
- Đọc transaction từ Oracle Package
- Hỗ trợ pagination với pageSize động
- PageSize được đọc từ JobSetting, có thể thay đổi động

### 3. Ví dụ sử dụng

```java
@Autowired
private OracleTransactionService oracleTransactionService;

// Lấy 10 rows đầu tiên
List<Transaction> transactions = oracleTransactionService.getTransactions10();

// Lấy N rows với offset
List<Transaction> transactions = oracleTransactionService.getTransactions(10, 0);

// Đếm tổng số transaction
long count = oracleTransactionService.countTransactions();
```

## So sánh các phương pháp

| Phương pháp | Ưu điểm | Nhược điểm | Khi nào sử dụng |
|------------|---------|------------|-----------------|
| **Procedure với REF CURSOR** | Linh hoạt, có thể xử lý logic phức tạp | Cần xử lý cursor trong code | Khi cần xử lý logic phức tạp trong package |
| **Pipelined Function** | Dễ sử dụng, có thể query trực tiếp | Không thể xử lý logic phức tạp | Khi cần query trực tiếp như table |
| **Function** | Đơn giản, trả về giá trị đơn | Chỉ trả về một giá trị | Khi cần tính toán, đếm số lượng |

## Cấu hình Database

### Oracle Database

Cần cấu hình datasource trong `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: your_username
    password: your_password
    driver-class-name: oracle.jdbc.OracleDriver
```

### H2 Database (Testing)

H2 không hỗ trợ Oracle Package, nhưng có thể test với H2 nếu cần:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:batchdb;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
```

**Lưu ý**: H2 chỉ hỗ trợ một phần Oracle syntax, không hỗ trợ đầy đủ Oracle Package. Để test đầy đủ, cần sử dụng Oracle Database.

## Troubleshooting

### Lỗi: Package không tồn tại

```
ORA-04043: object TRANSACTION_PKG does not exist
```

**Giải pháp**:
1. Kiểm tra package đã được tạo chưa
2. Kiểm tra user có quyền execute package không
3. Grant quyền: `GRANT EXECUTE ON TRANSACTION_PKG TO your_user;`

### Lỗi: REF CURSOR không được hỗ trợ

```
ORA-00932: inconsistent datatypes
```

**Giải pháp**:
1. Kiểm tra Oracle JDBC driver version (cần 12c trở lên)
2. Kiểm tra cách gọi procedure trong Java code

### Lỗi: Pipelined function không hoạt động

```
ORA-00904: invalid identifier
```

**Giải pháp**:
1. Kiểm tra Oracle version (cần 12c trở lên)
2. Kiểm tra syntax pipelined function

## Best Practices

1. **Sử dụng Index**: Đảm bảo có index trên `create_date` để tối ưu performance
2. **Pagination**: Sử dụng `ROW_NUMBER()` hoặc `FETCH FIRST N ROWS ONLY` để pagination
3. **Error Handling**: Xử lý exception trong package và Java code
4. **Logging**: Log các thao tác quan trọng trong package
5. **Testing**: Test package trước khi deploy

## Kết luận

Oracle Package `TRANSACTION_PKG` cung cấp nhiều cách để lấy data từ bảng transaction:
- **Procedure với REF CURSOR**: Linh hoạt, xử lý logic phức tạp
- **Pipelined Function**: Dễ sử dụng, query trực tiếp
- **Function**: Đơn giản, trả về giá trị đơn

Java code có thể gọi package này thông qua `OracleTransactionService` và sử dụng trong Spring Batch với `OracleTransactionReader`.

