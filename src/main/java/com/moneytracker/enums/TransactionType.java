package com.moneytracker.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    INCOME("INCOME"),
    EXPENSE("EXPENSE");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TransactionType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        for (TransactionType type : TransactionType.values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid transaction type: " + value +
                ". Valid values are: INCOME, EXPENSE");
    }

    @Override
    public String toString() {
        return this.value;
    }
}