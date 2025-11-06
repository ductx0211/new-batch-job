# Hướng dẫn cấu hình Java 21

## Đã cấu hình JAVA_HOME vĩnh viễn

JAVA_HOME đã được cấu hình trong `~/.bash_profile` để trỏ đến Java 21:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$PATH:$JAVA_HOME/bin
```

## Cách sử dụng

### 1. Reload shell profile (sau khi mở terminal mới)
```bash
source ~/.bash_profile
```

### 2. Kiểm tra Java version
```bash
java -version
# Kết quả mong đợi: java version "21.0.8"
```

### 3. Kiểm tra Maven sử dụng Java nào
```bash
mvn -version
# Kết quả mong đợi: Java version: 21.0.8
```

### 4. Chạy ứng dụng
```bash
mvn clean compile
mvn spring-boot:run
```

## Nếu cần chuyển về Java khác

### Chuyển về Java 11
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.16.1.jdk/Contents/Home
```

### Chuyển về Java 8
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_341.jdk/Contents/Home
```

### Xem tất cả Java đã cài đặt
```bash
/usr/libexec/java_home -V
```

## Lưu ý

- Sau khi cập nhật `.bash_profile`, cần mở terminal mới hoặc chạy `source ~/.bash_profile`
- File backup được lưu tại: `~/.bash_profile.backup`
- Để cấu hình vĩnh viễn, sửa file `~/.bash_profile` và thêm các dòng export JAVA_HOME

