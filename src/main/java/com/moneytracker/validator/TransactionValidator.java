// TransactionValidator.java - Custom Validation Logic
package com.moneytracker.validator;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionValidator {

    public List<String> validateTransaction(TransactionDTO transactionDTO) {
        List<String> errors = new ArrayList<>();

        // Amount validation
        if (transactionDTO.getAmount() == null) {
            errors.add("Amount is required");
        } else if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Amount must be greater than zero");
        } else if (transactionDTO.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            errors.add("Amount cannot exceed 1,000,000");
        }

        // Description validation
        if (transactionDTO.getDescription() == null || transactionDTO.getDescription().trim().isEmpty()) {
            errors.add("Description is required");
        } else if (transactionDTO.getDescription().length() > 255) {
            errors.add("Description cannot exceed 255 characters");
        }

        // Transaction type validation
        if (transactionDTO.getTransactionType() == null) {
            errors.add("Transaction type is required");
        }

        // Category validation (optional but if provided, should be valid)
        if (transactionDTO.getCategory() != null && transactionDTO.getCategory().length() > 50) {
            errors.add("Category cannot exceed 50 characters");
        }

        // Business rule validations
        if (transactionDTO.getTransactionType() == TransactionType.TRANSFER &&
                transactionDTO.getAmount() != null &&
                transactionDTO.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            errors.add("Transfer amount cannot exceed 100,000");
        }

        return errors;
    }

    public boolean isValidCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return true; // Category is optional
        }

        // Define valid categories
        List<String> validCategories = List.of(
                "Food & Dining", "Transportation", "Shopping", "Entertainment",
                "Bills & Utilities", "Healthcare", "Education", "Travel",
                "Groceries", "Gas", "Other"
        );

        return validCategories.contains(category);
    }
}
