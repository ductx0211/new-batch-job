-- ============================================================================
-- Oracle Database Scripts
-- Tạo các bảng cần thiết cho Batch Job Application
-- ============================================================================

-- 1. Tạo bảng TRANSACTION
CREATE TABLE transaction (
    id NUMBER(19) PRIMARY KEY,
    branch VARCHAR2(100),
    name VARCHAR2(255),
    amount NUMBER(19, 2),
    create_date TIMESTAMP,
    status VARCHAR2(50) DEFAULT NULL
);

-- Tạo sequence cho transaction id
CREATE SEQUENCE transaction_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Tạo trigger để auto increment id
CREATE OR REPLACE TRIGGER transaction_trigger
    BEFORE INSERT ON transaction
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT transaction_seq.NEXTVAL INTO :NEW.id FROM DUAL;
    END IF;
END;
/

-- Tạo index cho performance
CREATE INDEX idx_transaction_branch ON transaction(branch);
CREATE INDEX idx_transaction_create_date ON transaction(create_date);
CREATE INDEX idx_transaction_status ON transaction(status);

-- 2. Tạo bảng BATCH_JOB_SETTING (nếu chưa có)
CREATE TABLE batch_job_setting (
    job_name VARCHAR2(50) PRIMARY KEY,
    description VARCHAR2(255),
    status NUMBER,
    created_by VARCHAR2(50),
    created_date TIMESTAMP,
    updated_by VARCHAR2(50),
    updated_date TIMESTAMP,
    STEP_EXECUTION_ID NUMBER,
    JOB_EXECUTION_ID NUMBER,
    START_TIME TIMESTAMP,
    END_TIME TIMESTAMP,
    PARAMS VARCHAR2(4000)
);

-- 3. Tạo bảng JOB_LOG (nếu chưa có)
CREATE TABLE job_log (
    id NUMBER(19) PRIMARY KEY,
    job_name VARCHAR2(50),
    log_type VARCHAR2(50),
    message VARCHAR2(4000),
    note CLOB,
    created_by VARCHAR2(50),
    created_date TIMESTAMP,
    STEP_EXECUTION_ID NUMBER,
    JOB_EXECUTION_ID NUMBER
);

CREATE SEQUENCE job_log_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE OR REPLACE TRIGGER job_log_trigger
    BEFORE INSERT ON job_log
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT job_log_seq.NEXTVAL INTO :NEW.id FROM DUAL;
    END IF;
END;
/

-- 4. Tạo bảng JOB_LOG_RESULT (nếu chưa có)
CREATE TABLE job_log_result (
    id NUMBER(19) PRIMARY KEY,
    job_name VARCHAR2(50),
    row_run NUMBER,
    row_error NUMBER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    note VARCHAR2(4000),
    total NUMBER,
    SKIP_READER NUMBER,
    SKIP_PROCESSOR NUMBER,
    SKIP_WRITER NUMBER,
    SKIP_TOTAL NUMBER,
    SKIP_LIMIT NUMBER,
    STEP_EXECUTION_ID NUMBER,
    JOB_EXECUTION_ID NUMBER,
    status VARCHAR2(50),
    created_by VARCHAR2(50),
    created_date TIMESTAMP
);

CREATE SEQUENCE job_log_result_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE OR REPLACE TRIGGER job_log_result_trigger
    BEFORE INSERT ON job_log_result
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT job_log_result_seq.NEXTVAL INTO :NEW.id FROM DUAL;
    END IF;
END;
/

-- 5. Tạo bảng SHEDLOCK (cho ShedLock distributed locking)
CREATE TABLE shedlock (
    name VARCHAR2(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR2(255) NOT NULL,
    PRIMARY KEY (name)
);

-- 6. Insert sample data cho transaction (optional)
-- INSERT INTO transaction (branch, name, amount, create_date) VALUES
-- ('HN001', 'Transaction 1', 1000.50, CURRENT_TIMESTAMP);
-- INSERT INTO transaction (branch, name, amount, create_date) VALUES
-- ('HN001', 'Transaction 2', 2000.75, CURRENT_TIMESTAMP);
-- ... (thêm nhiều records nếu cần)

-- 7. Commit changes
COMMIT;

-- ============================================================================
-- Kiểm tra các bảng đã được tạo
-- ============================================================================
SELECT table_name FROM user_tables WHERE table_name IN (
    'TRANSACTION', 
    'BATCH_JOB_SETTING', 
    'JOB_LOG', 
    'JOB_LOG_RESULT', 
    'SHEDLOCK'
) ORDER BY table_name;

