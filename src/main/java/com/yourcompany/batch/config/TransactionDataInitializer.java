package com.yourcompany.batch.config;

import com.yourcompany.batch.domain.Transaction;
import com.yourcompany.batch.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Initializer để tạo sample transaction data
 * Chỉ chạy khi property 'batch.init.sample-data' = true
 */
@Component
@ConditionalOnProperty(name = "batch.init.sample-data", havingValue = "true", matchIfMissing = false)
public class TransactionDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(TransactionDataInitializer.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @PostConstruct
    public void init() {
        // Kiểm tra xem đã có data chưa
        long count = transactionRepository.count();
        if (count > 0) {
            log.info("Transaction table already has {} records. Skipping data initialization.", count);
            return;
        }

        log.info("Initializing sample transaction data...");
        List<Transaction> transactions = createSampleTransactions();
        transactionRepository.saveAll(transactions);
        log.info("Created {} sample transactions", transactions.size());
    }

    private List<Transaction> createSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String[] branches = {"HN001", "HN002", "HN003", "HCM001", "HCM002", "DN001"};

        // Tạo 25 sample transactions
        for (int i = 1; i <= 25; i++) {
            Transaction transaction = new Transaction();
            transaction.setBranch(branches[i % branches.length]);
            transaction.setName("Transaction " + i);
            transaction.setAmount(new BigDecimal(1000 * i + (i * 0.5)));
            transaction.setCreateDate(Instant.now().minusSeconds(3600 * (25 - i))); // Spread over time
            transactions.add(transaction);
        }

        return transactions;
    }
}

