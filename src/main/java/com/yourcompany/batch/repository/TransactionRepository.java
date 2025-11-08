package com.yourcompany.batch.repository;

import com.yourcompany.batch.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {

    /**
     * Đếm tổng số transaction sử dụng Oracle Package
     * Gọi function: TRANSACTION_PKG.count_transactions
     */
    @Query(value = "SELECT TRANSACTION_PKG.count_transactions() FROM DUAL", nativeQuery = true)
    long countTransactionsFromPackage();

}

