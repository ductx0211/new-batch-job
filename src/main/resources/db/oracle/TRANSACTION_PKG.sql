CREATE OR REPLACE PACKAGE TRANSACTION_PKG AS
    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR);
END TRANSACTION_PKG;
/

CREATE OR REPLACE PACKAGE BODY TRANSACTION_PKG AS

    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR) AS
        v_ids SYS.ODCINUMBERLIST := SYS.ODCINUMBERLIST();
    BEGIN
        -- Collect the IDs of the rows we are going to process (lock them to avoid race conditions)
        SELECT id
        BULK COLLECT INTO v_ids
        FROM transaction
        WHERE status IS NULL
        ORDER BY create_date ASC, id ASC
        FETCH FIRST 10 ROWS ONLY
        FOR UPDATE SKIP LOCKED;

        -- If no rows found, return empty cursor
        IF v_ids.COUNT = 0 THEN
            OPEN p_cursor FOR
                SELECT id,
                       branch,
                       name,
                       amount,
                       create_date,
                       status
                FROM transaction
                WHERE 1 = 0;
            RETURN;
        END IF;

        -- Return the locked rows
        OPEN p_cursor FOR
            SELECT id,
                   branch,
                   name,
                   amount,
                   create_date,
                   status
            FROM transaction
            WHERE id IN (SELECT COLUMN_VALUE FROM TABLE(v_ids))
            ORDER BY create_date ASC, id ASC;

        -- Update the status of exactly the locked rows to PENDING
        UPDATE transaction
        SET status = 'PENDING'
        WHERE id IN (SELECT COLUMN_VALUE FROM TABLE(v_ids));

        COMMIT;
    END get_transactions_10;

END TRANSACTION_PKG;
/
