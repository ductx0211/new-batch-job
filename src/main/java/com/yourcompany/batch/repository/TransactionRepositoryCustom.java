package com.yourcompany.batch.repository;

import com.yourcompany.batch.domain.Transaction;

import java.util.List;

/**
 * Custom repository interface cho các thao tác đặc biệt với Transaction
 */
public interface TransactionRepositoryCustom {

    /**
     * Lấy và lock một số lượng transaction nhất định (status IS NULL) và cập nhật status = 'PENDING'
     *
     * @param limit  số lượng tối đa cần lấy
     * @return danh sách Transaction đã được chuyển sang trạng thái PENDING
     */
    List<Transaction> fetchAndMarkTransactions(int limit);

    /**
     * Cập nhật status của transaction
     * 
     * @param transactionId ID của transaction cần cập nhật
     * @param status Status mới (JOB_PROCESSING, COMPLETED, ERROR)
     */
    void updateTransactionStatus(Long transactionId, String status);

    /**
     * Cập nhật status của nhiều transactions
     * 
     * @param transactionIds Danh sách IDs của transactions cần cập nhật
     * @param status Status mới (JOB_PROCESSING, COMPLETED, ERROR)
     */
    void updateTransactionsStatus(List<Long> transactionIds, String status);
}

