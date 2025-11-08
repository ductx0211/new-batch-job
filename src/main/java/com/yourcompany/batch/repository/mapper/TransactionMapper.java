package com.yourcompany.batch.repository.mapper;

import com.yourcompany.batch.domain.Transaction;
import com.yourcompany.batch.repository.dto.TransactionDTO;
import com.yourcompany.batch.repository.projection.TransactionProjection;
import org.springframework.stereotype.Component;

/**
 * Mapper để convert giữa TransactionProjection/TransactionDTO và Transaction entity
 */
@Component
public class TransactionMapper {

    /**
     * Convert TransactionProjection sang Transaction entity
     */
    public Transaction toEntity(TransactionProjection projection) {
        if (projection == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setId(projection.getId());
        transaction.setBranch(projection.getBranch());
        transaction.setName(projection.getName());
        transaction.setAmount(projection.getAmount());
        transaction.setCreateDate(projection.getCreateDate());
        transaction.setStatus(projection.getStatus());
        return transaction;
    }

    /**
     * Convert TransactionDTO sang Transaction entity
     */
    public Transaction toEntity(TransactionDTO dto) {
        if (dto == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        transaction.setBranch(dto.getBranch());
        transaction.setName(dto.getName());
        transaction.setAmount(dto.getAmount());
        transaction.setCreateDate(dto.getCreateDate());
        return transaction;
    }

    /**
     * Convert Transaction entity sang TransactionDTO
     */
    public TransactionDTO toDTO(Transaction entity) {
        if (entity == null) {
            return null;
        }

        TransactionDTO dto = new TransactionDTO();
        dto.setId(entity.getId());
        dto.setBranch(entity.getBranch());
        dto.setName(entity.getName());
        dto.setAmount(entity.getAmount());
        dto.setCreateDate(entity.getCreateDate());
        return dto;
    }
}

