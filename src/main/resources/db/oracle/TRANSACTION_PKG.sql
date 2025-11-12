CREATE OR REPLACE PACKAGE TRANSACTION_PKG AS
    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR);
END TRANSACTION_PKG;
/

CREATE OR REPLACE PACKAGE BODY TRANSACTION_PKG AS

    TYPE t_id_tab IS TABLE OF transaction.id%TYPE;

    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR) AS
        CURSOR c_transactions IS
            SELECT id,
                   branch,
                   name,
                   amount,
                   create_date,
                   status
            FROM transaction
            WHERE status IS NULL
            ORDER BY create_date ASC, id ASC
            FOR UPDATE SKIP LOCKED;

        v_row c_transactions%ROWTYPE;
        v_ids t_id_tab := t_id_tab();
        v_count PLS_INTEGER := 0;
    BEGIN
        OPEN c_transactions;

        LOOP
            FETCH c_transactions INTO v_row;
            EXIT WHEN c_transactions%NOTFOUND OR v_count >= 10;

            v_count := v_count + 1;
            v_ids.EXTEND;
            v_ids(v_count) := v_row.id;

            UPDATE transaction
            SET status = 'PENDING'
            WHERE CURRENT OF c_transactions;
        END LOOP;

        CLOSE c_transactions;

        IF v_count = 0 THEN
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


