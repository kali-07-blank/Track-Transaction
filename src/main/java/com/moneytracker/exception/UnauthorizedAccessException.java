package com.moneytracker.exception;

/**
 * Exception thrown when user tries to access unauthorized resources
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}