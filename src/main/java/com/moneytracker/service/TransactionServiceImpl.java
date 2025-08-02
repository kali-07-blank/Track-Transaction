package com.moneytracker.service;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummaryDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.entity.Transaction;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.repository.PersonRepository;
import com.moneytracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TransactionService interface
 * Handles all business logic related to Transaction operations
 * Demonstrates OOP principles: Encapsulation, Inheritance, Polymorphism, Abstraction
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final PersonRepository personRepository;

    /**
     * Constructor injection for dependencies (Dependency Injection principle)
     */
    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, PersonRepository personRepository) {
        this.transactionRepository = transactionRepository;
        this.personRepository = personRepository;
    }

    /**
     * Creates a new transaction with validation
     * Implements business rules and data integrity checks
     */
    @Override
    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        // Input validation (Defensive programming)
        validateTransactionData(transactionDTO);

        // Verify person exists (Business rule)
        Person person = personRepository.findById(transactionDTO.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + transactionDTO.getPersonId()));

        // Create entity from DTO (Data mapping)
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setTransactionType(transactionDTO.getTransactionType());
        transaction.setCategory(transactionDTO.getCategory());
        transaction.setPerson(person);

        // Set transaction date (use provided date or current time)
        if (transactionDTO.getTransactionDate() != null) {
            transaction.setTransactionDate(transactionDTO.getTransactionDate());
        } else {
            transaction.setTransactionDate(LocalDateTime.now());
        }

        // Apply business rules based on transaction type
        applyBusinessRules(transaction);

        // Persist entity
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Convert back to DTO (Abstraction)
        return convertToDTO(savedTransaction);
    }

    /**
     * Retrieves transaction by ID with error handling
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return convertToDTO(transaction);
    }

    /**
     * Retrieves all transactions for a specific person
     * Uses repository method with sorting
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPersonId(Long personId) {
        validatePersonExists(personId);
        return transactionRepository.findByPersonIdOrderByTransactionDateDesc(personId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves transactions filtered by person and transaction type
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPersonIdAndType(Long personId, TransactionType type) {
        validatePersonExists(personId);
        validateTransactionType(type);

        return transactionRepository.findByPersonIdAndTransactionTypeOrderByTransactionDateDesc(personId, type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves transactions filtered by person and category
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPersonIdAndCategory(Long personId, String category) {
        validatePersonExists(personId);
        validateCategory(category);

        return transactionRepository.findByPersonIdAndCategoryOrderByTransactionDateDesc(personId, category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves transactions within a date range
     * Implements temporal filtering with validation
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(Long personId, LocalDateTime startDate, LocalDateTime endDate) {
        validatePersonExists(personId);
        validateDateRange(startDate, endDate);

        return transactionRepository.findByPersonIdAndDateRange(personId, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates existing transaction with validation
     * Implements optimistic updates
     */
    @Override
    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO) {
        // Find existing transaction
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Validate input data
        validateTransactionDataForUpdate(transactionDTO);

        // Update entity fields
        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setDescription(transactionDTO.getDescription());
        existingTransaction.setTransactionType(transactionDTO.getTransactionType());
        existingTransaction.setCategory(transactionDTO.getCategory());

        // Update transaction date if provided
        if (transactionDTO.getTransactionDate() != null) {
            existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
        }

        // Apply business rules
        applyBusinessRules(existingTransaction);

        // Save and return updated entity
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        return convertToDTO(updatedTransaction);
    }

    /**
     * Deletes transaction by ID with existence check
     */
    @Override
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    /**
     * Generates transaction summary for a person
     * Implements financial calculations and reporting
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getTransactionSummary(Long personId) {
        validatePersonExists(personId);

        // Get aggregated data from repository
        BigDecimal totalIncome = transactionRepository.getTotalIncomeByPersonId(personId);
        BigDecimal totalExpenses = transactionRepository.getTotalExpensesByPersonId(personId);
        BigDecimal balance = transactionRepository.getBalanceByPersonId(personId);

        // Handle null values (defensive programming)
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        balance = balance != null ? balance : BigDecimal.ZERO;

        return new TransactionSummaryDTO(totalIncome, totalExpenses, balance);
    }

    /**
     * Retrieves distinct categories for a person
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getCategoriesByPersonId(Long personId) {
        validatePersonExists(personId);
        return transactionRepository.getDistinctCategoriesByPersonId(personId);
    }

    /**
     * Private helper method to validate person existence (Encapsulation)
     */
    private void validatePersonExists(Long personId) {
        if (personId == null) {
            throw new IllegalArgumentException("Person ID cannot be null");
        }
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException("Person not found with id: " + personId);
        }
    }

    /**
     * Private helper method for transaction data validation (Encapsulation)
     */
    private void validateTransactionData(TransactionDTO transactionDTO) {
        // Amount validation
        if (transactionDTO.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (transactionDTO.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("Amount cannot exceed 1,000,000");
        }

        // Description validation
        if (transactionDTO.getDescription() == null || transactionDTO.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (transactionDTO.getDescription().length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }

        // Transaction type validation
        if (transactionDTO.getTransactionType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        // Person ID validation
        if (transactionDTO.getPersonId() == null) {
            throw new IllegalArgumentException("Person ID is required");
        }

        // Category validation (optional but if provided, should be valid)
        if (transactionDTO.getCategory() != null && transactionDTO.getCategory().length() > 50) {
            throw new IllegalArgumentException("Category cannot exceed 50 characters");
        }
    }

    /**
     * Validation for update operations (person ID not required as it can't be changed)
     */
    private void validateTransactionDataForUpdate(TransactionDTO transactionDTO) {
        // Amount validation
        if (transactionDTO.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (transactionDTO.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("Amount cannot exceed 1,000,000");
        }

        // Description validation
        if (transactionDTO.getDescription() == null || transactionDTO.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (transactionDTO.getDescription().length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }

        // Transaction type validation
        if (transactionDTO.getTransactionType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        // Category validation
        if (transactionDTO.getCategory() != null && transactionDTO.getCategory().length() > 50) {
            throw new IllegalArgumentException("Category cannot exceed 50 characters");
        }
    }

    /**
     * Validates transaction type enum
     */
    private void validateTransactionType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
    }

    /**
     * Validates category parameter
     */
    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
    }

    /**
     * Validates date range parameters
     */
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (startDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
    }

    /**
     * Applies business rules based on transaction type and amount
     */
    private void applyBusinessRules(Transaction transaction) {
        // Business rule: Large transfer validation
        if (transaction.getTransactionType() == TransactionType.TRANSFER &&
                transaction.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            throw new IllegalArgumentException("Transfer amount cannot exceed 100,000");
        }

        // Business rule: Set default category for certain transaction types
        if (transaction.getCategory() == null || transaction.getCategory().trim().isEmpty()) {
            switch (transaction.getTransactionType()) {
                case INCOME:
                    transaction.setCategory("Income");
                    break;
                case EXPENSE:
                    transaction.setCategory("General Expense");
                    break;
                case TRANSFER:
                    transaction.setCategory("Transfer");
                    break;
            }
        }

        // Business rule: Validate transaction date is not too far in the future
        if (transaction.getTransactionDate().isAfter(LocalDateTime.now().plusDays(30))) {
            throw new IllegalArgumentException("Transaction date cannot be more than 30 days in the future");
        }
    }

    /**
     * Converts Transaction entity to TransactionDTO (Data mapping)
     * Implements abstraction by hiding internal entity structure
     */
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setCategory(transaction.getCategory());
        dto.setPersonId(transaction.getPerson().getId());
        return dto;
    }
}