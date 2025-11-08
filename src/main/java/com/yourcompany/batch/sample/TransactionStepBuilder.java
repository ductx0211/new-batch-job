package com.yourcompany.batch.sample;

import com.yourcompany.batch.batch.reader.TransactionReader;
import com.yourcompany.batch.batch.step.AbstractStepBuilder;
import com.yourcompany.batch.domain.Transaction;
import com.yourcompany.batch.domain.enumeration.TransactionStatus;
import com.yourcompany.batch.repository.TransactionRepository;
import com.yourcompany.batch.service.ExternalApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Step Builder để xử lý Transaction
 * Đọc 10 transactions mỗi lần từ database và xử lý
 * Sau khi xử lý thành công, cập nhật status = 'COMPLETED'
 * Nếu có lỗi, cập nhật status = 'ERROR'
 */
@Component
public class TransactionStepBuilder extends AbstractStepBuilder<Transaction, Transaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionStepBuilder.class);

    @Autowired
    private TransactionReader transactionReader;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ExternalApiService externalApiService;

    @Override
    protected String getStepName() {
        return "TRANSACTION_PROCESSING_STEP";
    }

    @Override
    protected int countTotalItems() {
        // Đếm tổng số transaction trong database
        long totalCount = transactionReader.getTotalCount();
        log.info("Total transactions to process: {}", totalCount);
        return (int) totalCount;
    }

    @Override
    protected String getLogMessageAtStart() {
        return "Starting transaction processing step - processing transactions from database";
    }

    @Override
    protected ItemReader<Transaction> reader() {
        // Initialize reader - limit đã được set trong Oracle Package (10 rows)
        transactionReader.initialize();
        log.info("TransactionReader initialized for reading transactions from Oracle Package with limit: {}", 
            transactionReader.getPageSize());
        return transactionReader;
    }

    @Override
    protected ItemProcessor<Transaction, Transaction> processor() {
        return transaction -> {
            try {
                // Xử lý transaction
                log.debug("Processing transaction: id={}, branch={}, name={}, amount={}", 
                    transaction.getId(), transaction.getBranch(), transaction.getName(), transaction.getAmount());
                
                // Ví dụ: Tính toán hoặc validate transaction
                // Có thể thêm logic xử lý ở đây
                if (transaction.getAmount() == null) {
                    log.warn("Transaction {} has null amount", transaction.getId());
                }
                
                // Gọi API bên ngoài để xử lý transaction
                boolean apiSuccess = externalApiService.processTransaction(
                    transaction.getId(), 
                    transaction.getBranch()
                );
                
                if (!apiSuccess) {
                    // Nếu call API thất bại, cập nhật status = 'ERROR' và throw exception
                    log.error("External API call failed for transaction id={}, branch={}", 
                        transaction.getId(), transaction.getBranch());
                    transactionRepository.updateTransactionStatus(
                        transaction.getId(), 
                        TransactionStatus.ERROR.getValue()
                    );
                    throw new RuntimeException("External API call failed for transaction id=" + transaction.getId());
                }
                
                log.info("Successfully processed transaction via external API: id={}, branch={}", 
                    transaction.getId(), transaction.getBranch());
                
                // Có thể modify transaction nếu cần
                // transaction.setProcessed(true);
                
                return transaction;
            } catch (Exception e) {
                // Nếu có lỗi trong processor, cập nhật status = 'ERROR'
                log.error("Error processing transaction id={}: {}", transaction.getId(), e.getMessage(), e);
                
                // Đảm bảo status được cập nhật thành ERROR (nếu chưa được cập nhật)
                try {
                    transactionRepository.updateTransactionStatus(
                        transaction.getId(), 
                        TransactionStatus.ERROR.getValue()
                    );
                } catch (Exception updateError) {
                    log.error("Error updating transaction status to ERROR: {}", updateError.getMessage(), updateError);
                }
                
                throw e; // Re-throw để Spring Batch skip item này
            }
        };
    }

    @Override
    protected ItemWriter<Transaction> writer() {
        return transactions -> {
            try {
                // Ghi kết quả xử lý
                List<Long> processedIds = new ArrayList<>();
                
                for (Transaction transaction : transactions) {
                    log.info("Writing processed transaction: id={}, branch={}, name={}, amount={}", 
                        transaction.getId(), transaction.getBranch(), transaction.getName(), transaction.getAmount());
                    
                    processedIds.add(transaction.getId());
                }
                
                // Cập nhật status = 'COMPLETED' cho các transactions đã xử lý thành công
                if (!processedIds.isEmpty()) {
                    transactionRepository.updateTransactionsStatus(processedIds, TransactionStatus.COMPLETED.getValue());
                    log.info("Updated {} transactions status to COMPLETED", processedIds.size());
                }
                
                log.info("Written {} transactions successfully", transactions.size());
            } catch (Exception e) {
                log.error("Error writing transactions: {}", e.getMessage(), e);
                
                // Cập nhật status = 'ERROR' cho các transactions bị lỗi
                List<Long> errorIds = new ArrayList<>();
                for (Transaction transaction : transactions) {
                    errorIds.add(transaction.getId());
                }
                
                if (!errorIds.isEmpty()) {
                    try {
                        transactionRepository.updateTransactionsStatus(errorIds, TransactionStatus.ERROR.getValue());
                        log.info("Updated {} transactions status to ERROR due to write failure", errorIds.size());
                    } catch (Exception updateError) {
                        log.error("Error updating transaction status to ERROR: {}", updateError.getMessage(), updateError);
                    }
                }
                
                throw e; // Re-throw để Spring Batch xử lý lỗi
            }
        };
    }

    @Override
    protected void beforeStep(org.springframework.batch.core.StepExecution stepExecution) {
        super.beforeStep(stepExecution);
        addJobLog(com.yourcompany.batch.domain.enumeration.LogTypeEnum.INFO, 
            "Transaction processing step started", 
            "Will process transactions from Oracle Package in batches of 10 (limit set in package)");
    }

    @Override
    protected void afterStep(org.springframework.batch.core.StepExecution stepExecution) {
        super.afterStep(stepExecution);
        addJobLog(com.yourcompany.batch.domain.enumeration.LogTypeEnum.INFO, 
            "Transaction processing step completed", 
            "Processed " + getNumberOfProcessed() + " transactions");
    }
}

