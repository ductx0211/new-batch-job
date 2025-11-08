package com.yourcompany.batch.batch.reader;

import com.yourcompany.batch.domain.Transaction;
import com.yourcompany.batch.repository.TransactionRepository;
import com.yourcompany.batch.repository.mapper.TransactionMapper;
import com.yourcompany.batch.repository.projection.TransactionProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reader để đọc Transaction từ Oracle Package
 * Sử dụng TransactionRepository với projection để hứng dữ liệu từ Oracle Package TRANSACTION_PKG
 * Package luôn lấy 10 rows đầu tiên theo thời gian xa nhất (create_date ASC)
 * Không cần truyền offset, sử dụng lastProcessedId để track vị trí đã xử lý
 */
@Component
public class TransactionReader implements ItemReader<Transaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionReader.class);
    private static final int PACKAGE_LIMIT = 10; // Limit đã được set trong Oracle Package

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    private List<Transaction> currentPageData;
    private int currentIndex = 0;
    private boolean initialized = false;

    /**
     * Khởi tạo reader
     */
    public void initialize() {
        reset();
        log.info("TransactionReader initialized - Using Oracle Package with limit: {}", PACKAGE_LIMIT);
    }

    @Override
    public Transaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
            initialized = true;
        }

        // Nếu đã hết data trong trang hiện tại, lấy trang tiếp theo từ Oracle Package
        if (currentPageData == null || currentIndex >= currentPageData.size()) {
            currentPageData = loadNextPage();
            currentIndex = 0;
            
            // Nếu không còn data, return null để kết thúc reading
            if (currentPageData == null || currentPageData.isEmpty()) {
                log.info("No more transactions to read (status IS NULL)");
                return null;
            }
        }

        // Lấy transaction tiếp theo từ trang hiện tại
        Transaction transaction = currentPageData.get(currentIndex);
        currentIndex++;
        
        log.debug("Reading transaction from Oracle Package: id={}, branch={}, name={}, amount={}", 
            transaction.getId(), transaction.getBranch(), transaction.getName(), transaction.getAmount());
        
        return transaction;
    }

    /**
     * Load trang tiếp theo từ Oracle Package
     * Package sẽ luôn lấy 10 rows có status IS NULL và create_date cũ nhất
     * Sau khi lấy, package sẽ tự động cập nhật status = 'JOB_PROCESSING'
     * Sử dụng projection để hứng dữ liệu và mapper để convert sang Transaction entity
     */
    private List<Transaction> loadNextPage() {
        try {
            // Lấy 10 rows có status IS NULL (package sẽ tự động cập nhật status = 'JOB_PROCESSING')
            List<TransactionProjection> projections = transactionRepository.getTransactions10Projection();
            
            if (projections != null && !projections.isEmpty()) {
                // Convert projection sang Transaction entity sử dụng mapper
                List<Transaction> transactions = projections.stream()
                    .map(transactionMapper::toEntity)
                    .collect(Collectors.toList());
                
                log.info("Loaded page from Oracle Package via Repository with projection: {} transactions (limit={} fixed in package, status updated to JOB_PROCESSING by package)", 
                    transactions.size(), PACKAGE_LIMIT);
                return transactions;
            } else {
                log.info("No more pages to load from Oracle Package (no transactions with status IS NULL)");
                return null;
            }
        } catch (Exception e) {
            log.error("Error loading page from Oracle Package via Repository: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading transaction page from Oracle Package via Repository", e);
        }
    }

    /**
     * Reset reader để có thể đọc lại từ đầu
     */
    public void reset() {
        log.info("Resetting TransactionReader");
        currentIndex = 0;
        currentPageData = null;
        initialized = false;
    }

    /**
     * Get total count của transactions sử dụng Oracle Package qua Repository
     */
    public long getTotalCount() {
        return transactionRepository.countTransactionsFromPackage();
    }

    /**
     * Get package limit (đã được set trong Oracle Package)
     */
    public int getPageSize() {
        return PACKAGE_LIMIT;
    }
}
