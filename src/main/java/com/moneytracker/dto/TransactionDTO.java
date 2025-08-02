package com.moneytracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.moneytracker.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Transaction entity
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999999999.99", message = "Amount exceeds maximum limit")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 255, message = "Tags must not exceed 255 characters")
    private String tags;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @NotNull(message = "Person ID is required")
    private Long personId;

    private PersonDTO person;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long version;

    // Constructors
    public TransactionDTO() {}

    public TransactionDTO(BigDecimal amount, String description, TransactionType transactionType,
                          LocalDateTime transactionDate, String category, Long personId) {
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.category = category;
        this.personId = personId;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }

    public PersonDTO getPerson() { return person; }
    public void setPerson(PersonDTO person) { this.person = person; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}