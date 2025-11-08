package com.yourcompany.batch.repository;

import com.yourcompany.batch.repository.projection.TransactionProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom repository implementation để gọi Oracle Package với projection
 * Sử dụng EntityManager để thực thi native query và map kết quả sang projection
 */
@Repository
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TransactionProjection> getTransactions10Projection() {
        try {
            String sql = "SELECT id, branch, name, amount, create_date, status " +
                        "FROM TABLE(TRANSACTION_PKG.get_transactions_10_piped)";
            
            Query query = entityManager.createNativeQuery(sql);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            List<TransactionProjection> projections = new ArrayList<>();
            for (Object[] row : results) {
                TransactionProjection projection = createProjection(row);
                projections.add(projection);
            }
            
            log.info("Found {} transaction projections with status IS NULL (status will be updated to JOB_PROCESSING by package)", projections.size());
            return projections;
        } catch (Exception e) {
            log.error("Error getting transaction projections: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting transaction projections from Oracle Package", e);
        }
    }

    @Override
    public void updateTransactionStatus(Long transactionId, String status) {
        try {
            String sql = "BEGIN TRANSACTION_PKG.update_transaction_status(:transactionId, :status); END;";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("transactionId", transactionId);
            query.setParameter("status", status);
            
            query.executeUpdate();
            
            log.debug("Updated transaction id={} status to {}", transactionId, status);
        } catch (Exception e) {
            log.error("Error updating transaction status for id={} to {}: {}", transactionId, status, e.getMessage(), e);
            throw new RuntimeException("Error updating transaction status", e);
        }
    }

    @Override
    public void updateTransactionsStatus(List<Long> transactionIds, String status) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            return;
        }
        
        try {
            // Convert List<Long> to comma-separated string
            String idsString = transactionIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            
            String sql = "BEGIN TRANSACTION_PKG.update_transactions_status(:transactionIds, :status); END;";
            
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("transactionIds", idsString);
            query.setParameter("status", status);
            
            query.executeUpdate();
            
            log.debug("Updated {} transactions status to {}", transactionIds.size(), status);
        } catch (Exception e) {
            log.error("Error updating transactions status for {} ids to {}: {}", 
                transactionIds.size(), status, e.getMessage(), e);
            throw new RuntimeException("Error updating transactions status", e);
        }
    }

    /**
     * Tạo TransactionProjection từ row data
     */
    private TransactionProjection createProjection(Object[] row) {
        return new TransactionProjection() {
            @Override
            public Long getId() {
                return row[0] != null ? ((Number) row[0]).longValue() : null;
            }

            @Override
            public String getBranch() {
                return row[1] != null ? (String) row[1] : null;
            }

            @Override
            public String getName() {
                return row[2] != null ? (String) row[2] : null;
            }

            @Override
            public BigDecimal getAmount() {
                return row[3] != null ? (BigDecimal) row[3] : null;
            }

            @Override
            public Instant getCreateDate() {
                if (row[4] != null) {
                    if (row[4] instanceof Timestamp) {
                        return ((Timestamp) row[4]).toInstant();
                    } else if (row[4] instanceof java.sql.Date) {
                        return ((java.sql.Date) row[4]).toInstant();
                    } else if (row[4] instanceof Instant) {
                        return (Instant) row[4];
                    }
                }
                return null;
            }

            @Override
            public String getStatus() {
                return row.length > 5 && row[5] != null ? (String) row[5] : null;
            }
        };
    }
}

