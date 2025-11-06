-- ShedLock table để quản lý distributed locking
-- Table này sẽ được tạo tự động bởi ShedLock nếu chưa tồn tại
-- Nhưng có thể tạo thủ công để đảm bảo schema đúng

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

