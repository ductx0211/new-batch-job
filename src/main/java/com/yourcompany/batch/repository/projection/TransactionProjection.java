package com.yourcompany.batch.repository.projection;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Projection interface để hứng dữ liệu từ Oracle Package TRANSACTION_PKG
 * Sử dụng để map dữ liệu trả về từ pipelined function
 */
public interface TransactionProjection {

    /**
     * ID của transaction
     */
    Long getId();

    /**
     * Chi nhánh
     */
    String getBranch();

    /**
     * Tên transaction
     */
    String getName();

    /**
     * Số tiền
     */
    BigDecimal getAmount();

    /**
     * Ngày tạo
     */
    Instant getCreateDate();

    /**
     * Trạng thái (JOB_PROCESSING, COMPLETED, ERROR, NULL)
     */
    String getStatus();
}

