-- Create transaction table
CREATE TABLE IF NOT EXISTS transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch VARCHAR(100),
    name VARCHAR(255),
    amount DECIMAL(19, 2),
    create_date TIMESTAMP
);

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_transaction_branch ON transaction(branch);
CREATE INDEX IF NOT EXISTS idx_transaction_create_date ON transaction(create_date);

-- Sample data for testing
INSERT INTO transaction (branch, name, amount, create_date) VALUES
('HN001', 'Transaction 1', 1000.50, CURRENT_TIMESTAMP),
('HN001', 'Transaction 2', 2000.75, CURRENT_TIMESTAMP),
('HN002', 'Transaction 3', 3000.25, CURRENT_TIMESTAMP),
('HN002', 'Transaction 4', 4000.00, CURRENT_TIMESTAMP),
('HN003', 'Transaction 5', 5000.50, CURRENT_TIMESTAMP),
('HN003', 'Transaction 6', 6000.75, CURRENT_TIMESTAMP),
('HN001', 'Transaction 7', 7000.25, CURRENT_TIMESTAMP),
('HN002', 'Transaction 8', 8000.00, CURRENT_TIMESTAMP),
('HN003', 'Transaction 9', 9000.50, CURRENT_TIMESTAMP),
('HN001', 'Transaction 10', 10000.75, CURRENT_TIMESTAMP),
('HN002', 'Transaction 11', 11000.25, CURRENT_TIMESTAMP),
('HN003', 'Transaction 12', 12000.00, CURRENT_TIMESTAMP),
('HN001', 'Transaction 13', 13000.50, CURRENT_TIMESTAMP),
('HN002', 'Transaction 14', 14000.75, CURRENT_TIMESTAMP),
('HN003', 'Transaction 15', 15000.25, CURRENT_TIMESTAMP),
('HN001', 'Transaction 16', 16000.00, CURRENT_TIMESTAMP),
('HN002', 'Transaction 17', 17000.50, CURRENT_TIMESTAMP),
('HN003', 'Transaction 18', 18000.75, CURRENT_TIMESTAMP),
('HN001', 'Transaction 19', 19000.25, CURRENT_TIMESTAMP),
('HN002', 'Transaction 20', 20000.00, CURRENT_TIMESTAMP),
('HN003', 'Transaction 21', 21000.50, CURRENT_TIMESTAMP),
('HN001', 'Transaction 22', 22000.75, CURRENT_TIMESTAMP),
('HN002', 'Transaction 23', 23000.25, CURRENT_TIMESTAMP),
('HN003', 'Transaction 24', 24000.00, CURRENT_TIMESTAMP),
('HN001', 'Transaction 25', 25000.50, CURRENT_TIMESTAMP);

-- Oracle Database (nếu sử dụng Oracle)
/*
CREATE TABLE transaction (
    id NUMBER(19) PRIMARY KEY,
    branch VARCHAR2(100),
    name VARCHAR2(255),
    amount NUMBER(19, 2),
    create_date TIMESTAMP
);

CREATE SEQUENCE transaction_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX idx_transaction_branch ON transaction(branch);
CREATE INDEX idx_transaction_create_date ON transaction(create_date);
*/

