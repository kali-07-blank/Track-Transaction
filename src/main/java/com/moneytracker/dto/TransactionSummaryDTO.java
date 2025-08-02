package com.moneytracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Transaction Summary
 */
public class TransactionSummaryDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netAmount;
    private Long totalTransactions;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Constructors
    public TransactionSummaryDTO() {}

    public TransactionSummaryDTO(BigDecimal totalIncome, BigDecimal totalExpense,
                                 BigDecimal netAmount, Long totalTransactions) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netAmount = netAmount;
        this.totalTransactions = totalTransactions;
    }

    // Getters and setters
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public Long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Long totalTransactions) { this.totalTransactions = totalTransactions; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
}
