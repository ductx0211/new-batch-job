-- ============================================================================
-- Add status column to transaction table (if table already exists)
-- ============================================================================

-- Thêm cột status nếu chưa có
ALTER TABLE transaction ADD (
    status VARCHAR2(50) DEFAULT NULL
);

-- Tạo index cho status
CREATE INDEX idx_transaction_status ON transaction(status);

-- Update tất cả records hiện tại có status = NULL (nếu cần)
-- UPDATE transaction SET status = NULL WHERE status IS NULL;

COMMIT;

