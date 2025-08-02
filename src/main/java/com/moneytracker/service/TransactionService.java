package com.moneytracker.service;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummaryDTO;
import com.moneytracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Transaction operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
public interface TransactionService {

    /**
     * Create a new transaction
     * @param transactionDTO transaction data
     * @return created transaction DTO
     */
    TransactionDTO createTransaction(TransactionDTO transactionDTO);

    /**
     * Get transaction by ID
     * @param id transaction ID
     * @param personId person ID for security check
     * @return transaction DTO if found
     */
    Optional<TransactionDTO> getTransactionById(Long id, Long personId);

    /**
     * Update transaction
     * @param id transaction ID
     * @param transactionDTO updated transaction data
     * @param personId person ID for security check
     * @return updated transaction DTO
     */
    TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO, Long personId);

    /**
     * Delete transaction
     * @param id transaction ID
     * @param personId person ID for security check
     */
    void deleteTransaction(Long id, Long personId);

    /**
     * Get transactions by person with pagination
     * @param personId person ID
     * @param pageable pagination info
     * @return page of transaction DTOs
     */
    Page<TransactionDTO> getTransactionsByPersonId(Long personId, Pageable pageable);

    /**
     * Get transactions by person and date range
     * @param personId person ID
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination info
     * @return page of transaction DTOs
     */
    Page<TransactionDTO> getTransactionsByPersonIdAndDateRange(Long personId, LocalDateTime startDate,
                                                               LocalDateTime endDate, Pageable pageable);

    /**
     * Get transaction summary for person
     * @param personId person ID
     * @return transaction summary
     */
    TransactionSummaryDTO getTransactionSummary(Long personId);

    /**
     * Get transaction summary for person in date range
     * @param personId person ID
     * @param startDate start date
     * @param endDate end date
     * @return transaction summary
     */
    TransactionSummaryDTO getTransactionSummary(Long personId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get distinct categories for person
     * @param personId person ID
     * @return list of categories
     */
    List<String> getDistinctCategories(Long personId);

    /**
     * Convert entity to DTO
     * @param transaction transaction entity
     * @return transaction DTO
     */
    TransactionDTO convertToDTO(Transaction transaction);

    /**
     * Convert DTO to entity
     * @param transactionDTO transaction DTO
     * @return transaction entity
     */
    Transaction convertToEntity(TransactionDTO transactionDTO);
}