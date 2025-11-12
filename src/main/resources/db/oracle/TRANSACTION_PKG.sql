CREATE OR REPLACE PACKAGE TRANSACTION_PKG AS
    TYPE transaction_rec IS RECORD (
        id       webcsr_original.id%TYPE,
        location webcsr_original.location%TYPE,
        username webcsr_original.username%TYPE
    );

    TYPE transaction_tab IS TABLE OF transaction_rec;

    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR);

    FUNCTION get_transactions_10_piped
        RETURN transaction_tab PIPELINED;
END TRANSACTION_PKG;
/

CREATE OR REPLACE PACKAGE BODY TRANSACTION_PKG AS

    PROCEDURE get_transactions_10(p_cursor OUT SYS_REFCURSOR) AS
        CURSOR c_transactions IS
            SELECT id,
                   location,
                   username
            FROM webcsr_original
            WHERE status IS NULL
            ORDER BY id ASC
            FOR UPDATE SKIP LOCKED;

        v_row   c_transactions%ROWTYPE;
        v_ids   SYS.ODCINUMBERLIST := SYS.ODCINUMBERLIST();
        v_count PLS_INTEGER := 0;
    BEGIN
        OPEN c_transactions;

        LOOP
            FETCH c_transactions INTO v_row;
            EXIT WHEN c_transactions%NOTFOUND OR v_count >= 10;

            v_count := v_count + 1;
            v_ids.EXTEND;
            v_ids(v_count) := v_row.id;

            UPDATE webcsr_original
            SET status = 'PENDING'
            WHERE CURRENT OF c_transactions;
        END LOOP;

        CLOSE c_transactions;

        IF v_count = 0 THEN
            OPEN p_cursor FOR
                SELECT id,
                       location,
                       username,
                       status
                FROM webcsr_original
                WHERE 1 = 0;
            RETURN;
        END IF;

        OPEN p_cursor FOR
            SELECT id,
                   location,
                   username,
                   status
            FROM webcsr_original
            WHERE id IN (SELECT COLUMN_VALUE FROM TABLE(v_ids))
            ORDER BY id ASC;

        COMMIT;
    END get_transactions_10;

    FUNCTION get_transactions_10_piped
        RETURN transaction_tab PIPELINED AS
        CURSOR c_transactions IS
            SELECT id,
                   location,
                   username
            FROM webcsr_original
            WHERE status IS NULL
            ORDER BY id ASC
            FOR UPDATE SKIP LOCKED;

        v_row c_transactions%ROWTYPE;
        v_count PLS_INTEGER := 0;
        v_result transaction_rec;
    BEGIN
        OPEN c_transactions;

        LOOP
            FETCH c_transactions INTO v_row;
            EXIT WHEN c_transactions%NOTFOUND OR v_count >= 10;

            v_count := v_count + 1;

            UPDATE webcsr_original
            SET status = 'PENDING'
            WHERE CURRENT OF c_transactions;

            v_result.id := v_row.id;
            v_result.location := v_row.location;
            v_result.username := v_row.username;

            PIPE ROW(v_result);
        END LOOP;

        CLOSE c_transactions;

        COMMIT;

        RETURN;
    END get_transactions_10_piped;

END TRANSACTION_PKG;
/

-- TEST
DECLARE
    v_cursor SYS_REFCURSOR;
    v_id       webcsr_original.id%TYPE;
    v_location webcsr_original.location%TYPE;
    v_username webcsr_original.username%TYPE;
    v_status   webcsr_original.status%TYPE;
BEGIN
    TRANSACTION_PKG.get_transactions_10(v_cursor);

    LOOP
        FETCH v_cursor INTO v_id, v_location, v_username, v_status;
        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.put_line(
              'ID=' || v_id
           || ' | location=' || v_location
           || ' | username=' || v_username
           || ' | status(before)=' || NVL(v_status, '<NULL>')
        );
    END LOOP;

    CLOSE v_cursor;
END;
/


