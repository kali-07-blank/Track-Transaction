package com.moneytracker.repository;

import com.moneytracker.entity.Transaction;
import com.moneytracker.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific person
     * @param personId the person ID
     * @param pageable pagination information
     * @return page of transactions
     */
    Page<Transaction> findByPersonIdOrderByTransactionDateDesc(Long personId, Pageable pageable);

    /**
     * Find transactions by person and date range
     * @param personId the person ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param pageable pagination information
     * @return page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.person.id = :personId " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByPersonIdAndDateRange(@Param("personId") Long personId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 Pageable pageable);

    /**
     * Find transactions by person and category
     * @param personId the person ID
     * @param category the category
     * @param pageable pagination information
     * @return page of transactions
     */
    Page<Transaction> findByPersonIdAndCategoryContainingIgnoreCaseOrderByTransactionDateDesc(
            Long personId, String category, Pageable pageable);

    /**
     * Find transactions by person and transaction type
     * @param personId the person ID
     * @param transactionType the transaction type
     * @param pageable pagination information
     * @return page of transactions
     */
    Page<Transaction> findByPersonIdAndTransactionTypeOrderByTransactionDateDesc(
            Long personId, TransactionType transactionType, Pageable pageable);

    /**
     * Calculate total amount by person and transaction type
     * @param personId the person ID
     * @param transactionType the transaction type
     * @return total amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.person.id = :personId AND t.transactionType = :transactionType")
    BigDecimal sumAmountByPersonIdAndTransactionType(@Param("personId") Long personId,
                                                     @Param("transactionType") TransactionType transactionType);

    /**
     * Calculate total amount by person, transaction type and date range
     * @param personId the person ID
     * @param transactionType the transaction type
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return total amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.person.id = :personId AND t.transactionType = :transactionType " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByPersonIdAndTransactionTypeAndDateRange(
            @Param("personId") Long personId,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count transactions by person
     * @param personId the person ID
     * @return count of transactions
     */
    long countByPersonId(Long personId);

    /**
     * Count transactions by person and date range
     * @param personId the person ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return count of transactions
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.person.id = :personId " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    long countByPersonIdAndDateRange(@Param("personId") Long personId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find transaction by ID and person ID (for security)
     * @param id the transaction ID
     * @param personId the person ID
     * @return optional transaction
     */
    Optional<Transaction> findByIdAndPersonId(Long id, Long personId);

    /**
     * Delete transaction by ID and person ID (for security)
     * @param id the transaction ID
     * @param personId the person ID
     */
    void deleteByIdAndPersonId(Long id, Long personId);

    /**
     * Find distinct categories for a person
     * @param personId the person ID
     * @return list of categories
     */
    @Query("SELECT DISTINCT t.category FROM Transaction t WHERE t.person.id = :personId " +
            "AND t.category IS NOT NULL ORDER BY t.category")
    List<String> findDistinctCategoriesByPersonId(@Param("personId") Long personId);

    /**
     * Find monthly summary for a person
     * @param personId the person ID
     * @param year the year
     * @param month the month
     * @return list of monthly summary data
     */
    @Query("SELECT t.transactionType, COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t WHERE t.person.id = :personId " +
            "AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
            "GROUP BY t.transactionType")
    List<Object[]> findMonthlySummary(@Param("personId") Long personId,
                                      @Param("year") int year,
                                      @Param("month") int month);

    /**
     * Find category-wise summary for a person in date range
     * @param personId the person ID
     * @param startDate start date
     * @param endDate end date
     * @return list of category summary data
     */
    @Query("SELECT t.category, t.transactionType, COALESCE(SUM(t.amount), 0), COUNT(t) " +
            "FROM Transaction t WHERE t.person.id = :personId " +
            "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
            "AND t.category IS NOT NULL " +
            "GROUP BY t.category, t.transactionType " +
            "ORDER BY t.category, t.transactionType")
    List<Object[]> findCategorySummary(@Param("personId") Long personId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}