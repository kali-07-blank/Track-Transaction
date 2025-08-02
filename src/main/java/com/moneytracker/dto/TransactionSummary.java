// TransactionSummaryDTO.java
package com.moneytracker.dto;

import java.math.BigDecimal;

public class TransactionSummaryDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;

    public TransactionSummaryDTO() {}

    public TransactionSummaryDTO(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal balance) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.balance = balance;
    }

    // Getters and Setters
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
