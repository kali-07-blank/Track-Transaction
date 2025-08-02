package com.moneytracker.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse {
    private int status;
    private String error;      // Short summary
    private String message;    // Detailed explanation
    private LocalDateTime timestamp;
    private List<FieldError> fieldErrors;

    public ValidationErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = new ArrayList<>();
    }

    public ValidationErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public void addFieldError(String field, String message) {
        fieldErrors.add(new FieldError(field, message));
    }

    // Getters and setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public List<FieldError> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }

    public static class FieldError {
        private String field;
        private String message;

        public FieldError() {}

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
