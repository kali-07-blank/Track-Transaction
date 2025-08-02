package com.moneytracker.entity;

import com.moneytracker.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Transaction entity representing financial transactions in the money tracker system.
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_person_id", columnList = "person_id"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date"),
        @Index(name = "idx_transaction_type", columnList = "transaction_type"),
        @Index(name = "idx_transaction_category", columnList = "category"),
        @Index(name = "idx_transaction_person_date", columnList = "person_id, transaction_date")
})
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999999999.99", message = "Amount exceeds maximum limit")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Column(name = "description", nullable = false)
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    @Column(name = "category", length = 100)
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Column(name = "tags")
    @Size(max = 255, message = "Tags must not exceed 255 characters")
    private String tags;

    @Column(name = "location")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Column(name = "notes")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_person"))
    @NotNull(message = "Person is required")
    private Person person;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Version
    private Long version;

    // Constructors
    public Transaction() {
    }

    public Transaction(BigDecimal amount, String description, TransactionType transactionType,
                       LocalDateTime transactionDate, String category, Person person) {
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.category = category;
        this.person = person;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business methods
    /**
     * Checks if this transaction is an income transaction
     * @return true if transaction type is INCOME
     */
    public boolean isIncome() {
        return TransactionType.INCOME.equals(this.transactionType);
    }

    /**
     * Checks if this transaction is an expense transaction
     * @return true if transaction type is EXPENSE
     */
    public boolean isExpense() {
        return TransactionType.EXPENSE.equals(this.transactionType);
    }

    /**
     * Gets the signed amount based on transaction type
     * @return positive amount for income, negative for expense
     */
    public BigDecimal getSignedAmount() {
        return isIncome() ? amount : amount.negate();
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", transactionType=" + transactionType +
                ", transactionDate=" + transactionDate +
                ", category='" + category + '\'' +
                ", personId=" + (person != null ? person.getId() : null) +
                '}';
    }
}