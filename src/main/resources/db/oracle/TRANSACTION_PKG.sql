CREATE OR REPLACE PACKAGE TRANSACTION_PKG AS
    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR);
END TRANSACTION_PKG;
/

CREATE OR REPLACE PACKAGE BODY TRANSACTION_PKG AS

    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR) AS
    BEGIN
        OPEN p_cursor FOR
            WITH selected AS (
                SELECT id
                FROM transaction
                WHERE status IS NULL
                ORDER BY create_date ASC, id ASC
                FETCH FIRST 10 ROWS ONLY
            )
            SELECT t.id,
                   t.branch,
                   t.name,
                   t.amount,
                   t.create_date,
                   t.status
            FROM transaction t
            WHERE t.id IN (SELECT id FROM selected)
            ORDER BY t.create_date ASC, t.id ASC
            FOR UPDATE OF t.status SKIP LOCKED;

        UPDATE transaction
        SET status = 'PENDING'
        WHERE id IN (
            SELECT id
            FROM transaction
            WHERE status IS NULL
            ORDER BY create_date ASC, id ASC
            FETCH FIRST 10 ROWS ONLY
        );

        COMMIT;
    END get_transactions_10;

END TRANSACTION_PKG;
/
