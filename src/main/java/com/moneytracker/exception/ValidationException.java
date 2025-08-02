package com.moneytracker.exception;

import java.util.List;

/**
 * Exception thrown when validation fails
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}