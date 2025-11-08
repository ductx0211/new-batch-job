package com.yourcompany.batch.domain.enumeration;

/**
 * Transaction Status Enum
 */
public enum TransactionStatus {
    /**
     * Chưa xử lý (mặc định)
     */
    NULL(null),
    
    /**
     * Đang được job xử lý
     */
    JOB_PROCESSING("JOB_PROCESSING"),
    
    /**
     * Đã xử lý thành công
     */
    COMPLETED("COMPLETED"),
    
    /**
     * Xử lý lỗi
     */
    ERROR("ERROR");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionStatus fromValue(String value) {
        if (value == null) {
            return NULL;
        }
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.value != null && status.value.equals(value)) {
                return status;
            }
        }
        return NULL;
    }
}

