package com.moneytracker.repository;

import com.moneytracker.entity.Transaction;
import com.moneytracker.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions by person ID
     */
    List<Transaction> findByPersonIdOrderByTransactionDateDesc(Long personId);

    /**
     * Find transactions by person ID and transaction type
     */
    List<Transaction> findByPersonIdAndTransactionTypeOrderByTransactionDateDesc(Long personId, TransactionType transactionType);

    /**
     * Find transactions by person ID and category
     */
    List<Transaction> findByPersonIdAndCategoryOrderByTransactionDateDesc(Long personId, String category);

    /**
     * Find transactions by person ID within date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.person.id = :personId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByPersonIdAndDateRange(@Param("personId") Long personId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Get total income for a person
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.person.id = :personId AND t.transactionType = 'INCOME'")
    BigDecimal getTotalIncomeByPersonId(@Param("personId") Long personId);

    /**
     * Get total expenses for a person
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.person.id = :personId AND t.transactionType = 'EXPENSE'")
    BigDecimal getTotalExpensesByPersonId(@Param("personId") Long personId);

    /**
     * Get balance for a person (income - expenses)
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.transactionType = 'INCOME' THEN t.amount ELSE -t.amount END), 0) FROM Transaction t WHERE t.person.id = :personId")
    BigDecimal getBalanceByPersonId(@Param("personId") Long personId);

    /**
     * Get monthly summary for a person
     */
    @Query("SELECT t FROM Transaction t WHERE t.person.id = :personId AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month ORDER BY t.transactionDate DESC")
    List<Transaction> getMonthlyTransactions(@Param("personId") Long personId, @Param("year") int year, @Param("month") int month);

    /**
     * Get distinct categories for a person
     */
    @Query("SELECT DISTINCT t.category FROM Transaction t WHERE t.person.id = :personId AND t.category IS NOT NULL ORDER BY t.category")
    List<String> getDistinctCategoriesByPersonId(@Param("personId") Long personId);
}