package com.yourcompany.batch.repository.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO để hứng dữ liệu từ Oracle Package TRANSACTION_PKG
 * Sử dụng khi cần class-based projection thay vì interface
 */
public class TransactionDTO {

    private Long id;
    private String branch;
    private String name;
    private BigDecimal amount;
    private Instant createDate;

    public TransactionDTO() {
    }

    public TransactionDTO(Long id, String branch, String name, BigDecimal amount, Instant createDate) {
        this.id = id;
        this.branch = branch;
        this.name = name;
        this.amount = amount;
        this.createDate = createDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Instant createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
            "id=" + id +
            ", branch='" + branch + '\'' +
            ", name='" + name + '\'' +
            ", amount=" + amount +
            ", createDate=" + createDate +
            '}';
    }
}

