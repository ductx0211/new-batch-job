-- ============================================================================
-- Oracle Package: TRANSACTION_PKG
-- Mục đích: Lấy dữ liệu transaction từ bảng transaction
-- ============================================================================

-- Package Specification
CREATE OR REPLACE PACKAGE TRANSACTION_PKG AS
    -- Type để trả về transaction record
    TYPE transaction_rec IS RECORD (
        id NUMBER,
        branch VARCHAR2(100),
        name VARCHAR2(255),
        amount NUMBER(19, 2),
        create_date TIMESTAMP,
        status VARCHAR2(50)
    );
    
    -- Type để trả về table of transaction records
    TYPE transaction_tab IS TABLE OF transaction_rec;
    
    -- Type REF CURSOR để trả về kết quả
    TYPE transaction_cursor IS REF CURSOR RETURN transaction%ROWTYPE;
    
    -- Procedure: Lấy 10 rows transaction đầu tiên có status IS NULL theo thời gian xa nhất
    -- Lấy 10 rows có create_date cũ nhất và status IS NULL
    -- Sau khi lấy, cập nhật status = 'JOB_PROCESSING' cho các rows đã lấy
    -- p_cursor: OUT parameter để trả về kết quả
    PROCEDURE get_transactions_10(
        p_cursor OUT SYS_REFCURSOR
    );
    
    -- Function: Lấy 10 rows transaction đầu tiên có status IS NULL (sử dụng pipelined function)
    -- Lấy 10 rows có create_date cũ nhất và status IS NULL
    -- Sau khi lấy, cập nhật status = 'JOB_PROCESSING' cho các rows đã lấy
    -- Trả về table of transaction records
    FUNCTION get_transactions_10_piped
    RETURN transaction_tab PIPELINED;
    
    -- Procedure: Cập nhật status của transaction
    -- p_transaction_id: ID của transaction cần cập nhật
    -- p_status: Status mới (JOB_PROCESSING, COMPLETED, ERROR)
    PROCEDURE update_transaction_status(
        p_transaction_id IN NUMBER,
        p_status IN VARCHAR2
    );
    
    -- Procedure: Cập nhật status của nhiều transactions
    -- p_transaction_ids: Danh sách IDs của transactions cần cập nhật (comma-separated)
    -- p_status: Status mới (JOB_PROCESSING, COMPLETED, ERROR)
    PROCEDURE update_transactions_status(
        p_transaction_ids IN VARCHAR2,
        p_status IN VARCHAR2
    );
    
    -- Function: Đếm tổng số transaction
    FUNCTION count_transactions
    RETURN NUMBER;
    
END TRANSACTION_PKG;
/

-- Package Body
CREATE OR REPLACE PACKAGE BODY TRANSACTION_PKG AS
    
    -- Procedure: Lấy 10 rows transaction đầu tiên có status IS NULL theo thời gian xa nhất
    -- Sau khi lấy, cập nhật status = 'JOB_PROCESSING' cho các rows đã lấy
    PROCEDURE get_transactions_10(
        p_cursor OUT SYS_REFCURSOR
    ) AS
        v_ids VARCHAR2(4000);
    BEGIN
        -- Lấy 10 rows có status IS NULL và cập nhật status = 'JOB_PROCESSING'
        -- Sử dụng FOR UPDATE để lock rows trước khi update
        OPEN p_cursor FOR
            SELECT id, branch, name, amount, create_date, status
            FROM (
                SELECT id, branch, name, amount, create_date, status
                FROM transaction
                WHERE status IS NULL
                ORDER BY create_date ASC, id ASC
                FETCH FIRST 10 ROWS ONLY
                FOR UPDATE OF status NOWAIT
            );
        
        -- Cập nhật status = 'JOB_PROCESSING' cho các rows đã lấy
        UPDATE transaction
        SET status = 'JOB_PROCESSING'
        WHERE id IN (
            SELECT id FROM (
                SELECT id
                FROM transaction
                WHERE status IS NULL
                ORDER BY create_date ASC, id ASC
                FETCH FIRST 10 ROWS ONLY
            )
        );
        
        COMMIT;
    END get_transactions_10;
    
    -- Function: Lấy 10 rows transaction đầu tiên có status IS NULL (pipelined)
    -- Lấy 10 rows có create_date cũ nhất và status IS NULL
    -- Sau khi lấy, cập nhật status = 'JOB_PROCESSING' cho các rows đã lấy
    FUNCTION get_transactions_10_piped
    RETURN transaction_tab PIPELINED AS
        v_rec transaction_rec;
        v_ids VARCHAR2(4000) := '';
    BEGIN
        -- Lấy 10 rows có status IS NULL và collect IDs
        FOR v_row IN (
            SELECT id, branch, name, amount, create_date, status
            FROM transaction
            WHERE status IS NULL
            ORDER BY create_date ASC, id ASC
            FETCH FIRST 10 ROWS ONLY
            FOR UPDATE OF status NOWAIT
        ) LOOP
            -- Build list of IDs để update status sau
            IF v_ids IS NULL OR v_ids = '' THEN
                v_ids := TO_CHAR(v_row.id);
            ELSE
                v_ids := v_ids || ',' || TO_CHAR(v_row.id);
            END IF;
            
            -- Map data to record
            v_rec.id := v_row.id;
            v_rec.branch := v_row.branch;
            v_rec.name := v_row.name;
            v_rec.amount := v_row.amount;
            v_rec.create_date := v_row.create_date;
            v_rec.status := v_row.status;
            
            PIPE ROW(v_rec);
        END LOOP;
        
        -- Cập nhật status = 'JOB_PROCESSING' cho các rows đã lấy
        IF v_ids IS NOT NULL AND v_ids != '' THEN
            UPDATE transaction
            SET status = 'JOB_PROCESSING'
            WHERE id IN (
                SELECT TO_NUMBER(TRIM(REGEXP_SUBSTR(v_ids, '[^,]+', 1, LEVEL)))
                FROM DUAL
                CONNECT BY REGEXP_SUBSTR(v_ids, '[^,]+', 1, LEVEL) IS NOT NULL
            );
            COMMIT;
        END IF;
        
        RETURN;
    END get_transactions_10_piped;
    
    -- Procedure: Cập nhật status của transaction
    PROCEDURE update_transaction_status(
        p_transaction_id IN NUMBER,
        p_status IN VARCHAR2
    ) AS
    BEGIN
        UPDATE transaction
        SET status = p_status
        WHERE id = p_transaction_id;
        
        COMMIT;
    END update_transaction_status;
    
    -- Procedure: Cập nhật status của nhiều transactions
    PROCEDURE update_transactions_status(
        p_transaction_ids IN VARCHAR2,
        p_status IN VARCHAR2
    ) AS
    BEGIN
        IF p_transaction_ids IS NOT NULL AND p_transaction_ids != '' THEN
            UPDATE transaction
            SET status = p_status
            WHERE id IN (
                SELECT TO_NUMBER(TRIM(REGEXP_SUBSTR(p_transaction_ids, '[^,]+', 1, LEVEL)))
                FROM DUAL
                CONNECT BY REGEXP_SUBSTR(p_transaction_ids, '[^,]+', 1, LEVEL) IS NOT NULL
            );
            COMMIT;
        END IF;
    END update_transactions_status;
    
    -- Function: Đếm tổng số transaction có status IS NULL
    FUNCTION count_transactions
    RETURN NUMBER AS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM transaction
        WHERE status IS NULL;
        RETURN v_count;
    END count_transactions;
    
END TRANSACTION_PKG;
/

-- Grant permissions (nếu cần)
-- GRANT EXECUTE ON TRANSACTION_PKG TO your_user;

-- Test Package
-- ============================================================================
-- Test 1: Lấy 10 rows đầu tiên bằng procedure
-- ============================================================================
/*
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

-- ============================================================================
-- Test 2: Lấy 10 rows tiếp theo sau ID đã xử lý bằng procedure
-- ============================================================================
/*
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id NUMBER;
    v_branch VARCHAR2(100);
    v_name VARCHAR2(255);
    v_amount NUMBER(19, 2);
    v_create_date TIMESTAMP;
    v_last_processed_id NUMBER := 10; -- ID của transaction cuối cùng đã xử lý
BEGIN
    TRANSACTION_PKG.get_transactions_10_after_id(v_last_processed_id, v_cursor);
    
    LOOP
        FETCH v_cursor INTO v_id, v_branch, v_name, v_amount, v_create_date;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('ID: ' || v_id || ', Branch: ' || v_branch || ', Name: ' || v_name || ', Amount: ' || v_amount);
    END LOOP;
    
    CLOSE v_cursor;
END;
/

-- ============================================================================
-- Test 3: Lấy 10 rows đầu tiên bằng pipelined function
-- ============================================================================
/*
SELECT * FROM TABLE(TRANSACTION_PKG.get_transactions_10_piped);
/

-- ============================================================================
-- Test 4: Lấy 10 rows tiếp theo sau ID đã xử lý bằng pipelined function
-- ============================================================================
/*
SELECT * FROM TABLE(TRANSACTION_PKG.get_transactions_10_after_id_piped(10));
/

-- ============================================================================
-- Test 5: Đếm tổng số transaction
-- ============================================================================
/*
SELECT TRANSACTION_PKG.count_transactions() AS total_count FROM DUAL;
/

