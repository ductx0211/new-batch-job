package com.yourcompany.batch.batch.reader;

import com.yourcompany.batch.domain.Transaction;
import com.yourcompany.batch.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * Reader để đọc Transaction trực tiếp từ database thông qua JPA repository
 * Mỗi lần đọc sẽ lấy tối đa 10 rows có status IS NULL, đồng thời cập nhật status = 'PENDING'
 * nhằm tránh các job khác lấy trùng data.
 */
@Component
public class TransactionReader implements ItemReader<Transaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionReader.class);
    private static final int FETCH_LIMIT = 10;

    @Autowired
    private TransactionRepository transactionRepository;

    private List<Transaction> currentPageData;
    private int currentIndex = 0;
    private boolean initialized = false;
    private boolean hasLoadedData = false; // Flag để đảm bảo chỉ load một lần 10 items

    /**
     * Khởi tạo reader
     */
    public void initialize() {
        reset();
        log.info("TransactionReader initialized - Using JPA repository with fetch limit: {}", FETCH_LIMIT);
    }

    @Override
    public Transaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
            initialized = true;
        }

        // Chỉ load data một lần duy nhất (10 items)
        // Sau đó return null để dừng việc đọc thêm
        if (!hasLoadedData) {
            currentPageData = loadNextPage();
            hasLoadedData = true;
            currentIndex = 0;
            
            // Nếu không còn data, return null để kết thúc reading
            if (currentPageData == null || currentPageData.isEmpty()) {
                log.info("No more transactions to read (status IS NULL)");
                return null;
            }
        }

        // Nếu đã hết data trong trang hiện tại, return null để kết thúc
        if (currentIndex >= currentPageData.size()) {
            log.info("Finished reading {} transactions for this job execution", currentPageData.size());
            return null;
        }

        // Lấy transaction tiếp theo từ trang hiện tại
        Transaction transaction = currentPageData.get(currentIndex);
        currentIndex++;
        
        log.debug("Reading transaction from repository: id={}, branch={}, name={}, amount={}", 
            transaction.getId(), transaction.getBranch(), transaction.getName(), transaction.getAmount());
        
        return transaction;
    }

    /**
     * Load trang tiếp theo từ database thông qua repository
     * Mỗi lần gọi sẽ lock tối đa 10 rows có status IS NULL, cập nhật status = 'PENDING'
     * nhằm đảm bảo không job nào khác lấy trùng dữ liệu.
     */
    private List<Transaction> loadNextPage() {
        try {
            List<Transaction> transactions = transactionRepository.fetchAndMarkTransactions(FETCH_LIMIT);

            if (transactions != null && !transactions.isEmpty()) {
                log.info("Loaded {} transactions via JPA repository (status updated to PENDING)", transactions.size());
                return transactions;
            } else {
                log.info("No more transactions to load (no rows with status IS NULL)");
                return null;
            }
        } catch (Exception e) {
            log.error("Error loading transactions from repository: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading transaction page from repository", e);
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
        hasLoadedData = false; // Reset flag để có thể load lại trong lần chạy job tiếp theo
    }

    /**
     * Get total count của transactions còn chưa xử lý (status IS NULL)
     */
    public long getTotalCount() {
        return transactionRepository.countByStatusIsNull();
    }

    /**
     * Get fetch limit
     */
    public int getPageSize() {
        return FETCH_LIMIT;
    }
}
