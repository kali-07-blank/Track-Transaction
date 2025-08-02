package com.moneytracker.enums;

/**
 * Enumeration for transaction types in the money tracker system.
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
public enum TransactionType {
    /**
     * Income transaction - money coming in
     */
    INCOME("Income", "Money received"),

    /**
     * Expense transaction - money going out
     */
    EXPENSE("Expense", "Money spent"),

    /**
     * Transfer transaction - money moved between accounts
     */
    TRANSFER("Transfer", "Money transferred between accounts");

    private final String displayName;
    private final String description;

    TransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the human-readable display name for the transaction type
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of the transaction type
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the transaction type represents money coming in
     * @return true for INCOME and TRANSFER (when receiving)
     */
    public boolean isPositive() {
        return this == INCOME;
    }

    /**
     * Checks if the transaction type represents money going out
     * @return true for EXPENSE
     */
    public boolean isNegative() {
        return this == EXPENSE;
    }

    /**
     * Gets the multiplier for amount calculation
     * @return 1 for positive transactions, -1 for negative
     */
    public int getMultiplier() {
        return isPositive() ? 1 : -1;
    }
}