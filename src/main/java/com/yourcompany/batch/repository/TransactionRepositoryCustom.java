package com.yourcompany.batch.repository;

import com.yourcompany.batch.repository.projection.TransactionProjection;

import java.util.List;

/**
 * Custom repository interface để gọi Oracle Package với projection
 */
public interface TransactionRepositoryCustom {

    /**
     * Lấy 10 rows transaction đầu tiên có status IS NULL theo thời gian xa nhất (create_date ASC)
     * Sau khi lấy, package sẽ tự động cập nhật status = 'JOB_PROCESSING'
     * Sử dụng projection để hứng dữ liệu
     * 
     * @return Danh sách TransactionProjection (10 rows đầu tiên)
     */
    List<TransactionProjection> getTransactions10Projection();

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

