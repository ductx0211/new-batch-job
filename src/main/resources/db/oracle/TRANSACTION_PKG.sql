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

// TEST
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id        transaction.id%TYPE;
    v_branch    transaction.branch%TYPE;
    v_name      transaction.name%TYPE;
    v_amount    transaction.amount%TYPE;
    v_create    transaction.create_date%TYPE;
    v_status    transaction.status%TYPE;
BEGIN
    TRANSACTION_PKG.get_transactions_10(v_cursor);

    LOOP
        FETCH v_cursor
        INTO v_id, v_branch, v_name, v_amount, v_create, v_status;
        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.put_line(
            'ID=' || v_id ||
            ' | branch=' || v_branch ||
            ' | name=' || v_name ||
            ' | amount=' || v_amount ||
            ' | create_date=' || TO_CHAR(v_create, 'YYYY-MM-DD HH24:MI:SS') ||
            ' | status(before)=' || NVL(v_status, '<NULL>')
        );
    END LOOP;

    CLOSE v_cursor;
END;
/