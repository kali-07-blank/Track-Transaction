package com.moneytracker.exception;

/**
 * Exception thrown when authentication fails
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
