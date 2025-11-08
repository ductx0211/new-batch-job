# Hướng dẫn Migration từ H2 sang Oracle

## Tổng quan

Hướng dẫn này mô tả các bước để chuyển đổi project từ H2 database sang Oracle database.

## Thay đổi đã thực hiện

### 1. application.yml

**Trước (H2)**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:batchdb;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
```

**Sau (Oracle)**:
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: your_username
    password: your_password
    driver-class-name: oracle.jdbc.OracleDriver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    database-platform: org.hibernate.dialect.Oracle12cDialect
    hibernate:
      ddl-auto: none  # hoặc update cho development
```

### 2. pom.xml

**Trước (H2)**:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Sau (Oracle)**:
```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <version>21.9.0.0</version>
</dependency>
```

### 3. BatchConfiguration.java

**Trước (H2)**:
```java
factory.setDatabaseType("H2");
```

**Sau (Oracle)**:
```java
factory.setDatabaseType("ORACLE");
```

### 4. TransactionService.java

**Thay đổi SQL syntax**:
- **H2**: Sử dụng `LIMIT` và `OFFSET`
- **Oracle**: Sử dụng `ROW_NUMBER()` và subquery

**Ví dụ**:
```sql
-- H2
SELECT * FROM transaction ORDER BY create_date ASC LIMIT ? OFFSET ?

-- Oracle
SELECT * FROM (
    SELECT id, branch, name, amount, create_date,
           ROW_NUMBER() OVER (ORDER BY create_date ASC) AS rn
    FROM transaction
) WHERE rn > ? AND rn <= ?
ORDER BY create_date ASC
```

## Các bước migration

### Bước 1: Cài đặt Oracle Database

1. Cài đặt Oracle Database (Oracle XE hoặc Enterprise Edition)
2. Tạo database và user
3. Grant các quyền cần thiết

### Bước 2: Cập nhật cấu hình

1. **Cập nhật application.yml**:
   - Thay đổi `url`, `username`, `password`
   - Cập nhật `driver-class-name`
   - Cập nhật `database-platform`

2. **Cập nhật pom.xml**:
   - Comment H2 dependency
   - Uncomment Oracle dependency

3. **Cập nhật BatchConfiguration.java**:
   - Thay đổi `databaseType` từ "H2" sang "ORACLE"

### Bước 3: Tạo database schema

Chạy script SQL để tạo các bảng:

```bash
# Kết nối vào Oracle
sqlplus username/password@database

# Chạy script
@src/main/resources/db/oracle/create-tables-oracle.sql
```

Hoặc sử dụng SQL Developer, Toad, hoặc tool khác để chạy script.

### Bước 4: Tạo Oracle Package (nếu cần)

Nếu muốn sử dụng Oracle Package để lấy data:

```bash
# Chạy script Oracle Package
@src/main/resources/db/oracle/TRANSACTION_PKG.sql
```

### Bước 5: Cấu hình Spring Batch tables

Spring Batch sẽ tự động tạo các bảng metadata nếu:
- `spring.batch.jdbc.initialize-schema=always` (hoặc `always`)
- Hoặc chạy script SQL của Spring Batch

### Bước 6: Test connection

1. **Kiểm tra database connection**:
```bash
mvn spring-boot:run
```

2. **Kiểm tra logs**:
- Xem log để đảm bảo kết nối thành công
- Kiểm tra các bảng đã được tạo

3. **Test các endpoints**:
```bash
# Health check
curl http://localhost:8084/api/health

# Run job
curl -X POST http://localhost:8084/api/jobs/sample/run
```

## Cấu hình Oracle Database

### 1. Tạo User và Grant Permissions

```sql
-- Tạo user
CREATE USER batch_user IDENTIFIED BY batch_password;

-- Grant permissions
GRANT CONNECT, RESOURCE TO batch_user;
GRANT CREATE SESSION TO batch_user;
GRANT CREATE TABLE TO batch_user;
GRANT CREATE SEQUENCE TO batch_user;
GRANT CREATE TRIGGER TO batch_user;
GRANT CREATE PROCEDURE TO batch_user;
GRANT CREATE PACKAGE TO batch_user;
GRANT UNLIMITED TABLESPACE TO batch_user;
```

### 2. Connection String

**Format**:
```
jdbc:oracle:thin:@host:port:database
```

**Ví dụ**:
- Local: `jdbc:oracle:thin:@localhost:1521:XE`
- Remote: `jdbc:oracle:thin:@192.168.1.100:1521:ORCL`
- TNS: `jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=host)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=service)))`

### 3. Connection Pool Settings

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10      # Số lượng connections tối đa
      minimum-idle: 5            # Số lượng connections tối thiểu
      connection-timeout: 30000  # Timeout khi lấy connection (ms)
      idle-timeout: 600000       # Timeout khi connection idle (ms)
      max-lifetime: 1800000      # Thời gian sống tối đa của connection (ms)
```

## Khác biệt giữa H2 và Oracle

| Tính năng | H2 | Oracle |
|-----------|----|--------|
| **Pagination** | `LIMIT ? OFFSET ?` | `ROW_NUMBER() OVER ()` |
| **Auto Increment** | `AUTO_INCREMENT` | `SEQUENCE + TRIGGER` |
| **String Concatenation** | `CONCAT()` hoặc `+` | `CONCAT()` hoặc `\|\|` |
| **Date Functions** | `CURRENT_TIMESTAMP` | `CURRENT_TIMESTAMP` hoặc `SYSDATE` |
| **Case Sensitivity** | Case insensitive (default) | Case sensitive (quoted identifiers) |
| **Boolean** | `BOOLEAN` type | `NUMBER(1)` hoặc `CHAR(1)` |

## Troubleshooting

### Lỗi: ORA-12505: TNS:listener does not currently know of SID given in connect descriptor

**Nguyên nhân**: SID hoặc service name không đúng

**Giải pháp**:
1. Kiểm tra SID/service name trong Oracle
2. Sử dụng service name thay vì SID: `jdbc:oracle:thin:@localhost:1521/XE`

### Lỗi: ORA-00942: table or view does not exist

**Nguyên nhân**: Bảng chưa được tạo hoặc user không có quyền

**Giải pháp**:
1. Chạy script tạo bảng
2. Grant quyền SELECT, INSERT, UPDATE, DELETE cho user

### Lỗi: ORA-01400: cannot insert NULL into

**Nguyên nhân**: Sequence chưa được tạo hoặc trigger chưa được tạo

**Giải pháp**:
1. Tạo sequence
2. Tạo trigger để auto increment

### Lỗi: SQL syntax error với LIMIT/OFFSET

**Nguyên nhân**: Oracle không hỗ trợ LIMIT/OFFSET trực tiếp

**Giải pháp**:
1. Sử dụng `ROW_NUMBER()` và subquery
2. Hoặc sử dụng `FETCH FIRST N ROWS ONLY` (Oracle 12c+)

## Best Practices

1. **Sử dụng Connection Pool**: Cấu hình HikariCP để quản lý connections
2. **Index**: Tạo index trên các cột thường xuyên query (create_date, branch, etc.)
3. **Sequence**: Sử dụng sequence cho auto increment thay vì trigger nếu có thể
4. **Pagination**: Sử dụng `ROW_NUMBER()` cho pagination hiệu quả
5. **Transaction**: Quản lý transaction đúng cách để tránh deadlock

## Rollback (Quay lại H2)

Nếu cần quay lại H2:

1. **Cập nhật application.yml**:
   - Thay đổi lại datasource config về H2
   - Uncomment H2 console

2. **Cập nhật pom.xml**:
   - Uncomment H2 dependency
   - Comment Oracle dependency

3. **Cập nhật BatchConfiguration.java**:
   - Thay đổi `databaseType` về "H2"

4. **Cập nhật TransactionService.java**:
   - Thay đổi SQL về syntax H2 (LIMIT/OFFSET)

## Kết luận

Migration từ H2 sang Oracle đã hoàn tất. Các thay đổi chính:
- ✅ Cập nhật datasource configuration
- ✅ Cập nhật dependencies
- ✅ Cập nhật SQL syntax cho Oracle
- ✅ Tạo database schema scripts
- ✅ Cập nhật BatchConfiguration

Project hiện tại đã sẵn sàng để chạy với Oracle database.

