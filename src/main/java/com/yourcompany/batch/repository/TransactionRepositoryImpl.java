package com.yourcompany.batch.repository;

import com.yourcompany.batch.domain.Transaction;
import com.yourcompany.batch.domain.enumeration.TransactionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom repository implementation cho các thao tác đặc biệt với Transaction
 */
@Repository
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepositoryImpl.class);

    private static final String SELECT_IDS_SQL = """
        SELECT id
        FROM (
            SELECT t.id
            FROM transaction t
            WHERE t.status IS NULL
            ORDER BY t.create_date ASC, t.id ASC
        )
        WHERE ROWNUM <= :limit
        FOR UPDATE SKIP LOCKED
        """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Transaction> fetchAndMarkTransactions(int limit) {
        Query idQuery = entityManager.createNativeQuery(SELECT_IDS_SQL);
        idQuery.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Number> idNumbers = idQuery.getResultList();

        if (idNumbers == null || idNumbers.isEmpty()) {
            log.debug("No transactions available to lock (status IS NULL)");
            return List.of();
        }

        List<Long> ids = idNumbers.stream()
            .map(Number::longValue)
            .collect(Collectors.toCollection(ArrayList::new));

        int updated = entityManager.createQuery(
                "UPDATE Transaction t SET t.status = :status WHERE t.id IN :ids")
            .setParameter("status", TransactionStatus.PENDING.getValue())
            .setParameter("ids", ids)
            .executeUpdate();

        log.debug("Marked {} transactions as {}", updated, TransactionStatus.PENDING.getValue());

        // Flush và clear để đảm bảo update được commit ngay
        entityManager.flush();
        entityManager.clear();

        List<Transaction> transactions = entityManager.createQuery(
                "SELECT t FROM Transaction t WHERE t.id IN :ids ORDER BY t.createDate ASC, t.id ASC",
                Transaction.class)
            .setParameter("ids", ids)
            .getResultList();

        log.info("Fetched and marked {} transactions as PENDING (transaction committed immediately)", transactions.size());
        
        return transactions;
    }

    @Override
    @Transactional
    public void updateTransactionStatus(Long transactionId, String status) {
        if (transactionId == null) {
            return;
        }

        int updated = entityManager.createQuery(
                "UPDATE Transaction t SET t.status = :status WHERE t.id = :id")
            .setParameter("status", status)
            .setParameter("id", transactionId)
            .executeUpdate();

        log.debug("Updated transaction id={} to status {}, affectedRows={}", transactionId, status, updated);
    }

    @Override
    @Transactional
    public void updateTransactionsStatus(List<Long> transactionIds, String status) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            return;
        }

        int updated = entityManager.createQuery(
                "UPDATE Transaction t SET t.status = :status WHERE t.id IN :ids")
            .setParameter("status", status)
            .setParameter("ids", transactionIds)
            .executeUpdate();

        log.debug("Updated {} transactions to status {}", updated, status);
    }
}