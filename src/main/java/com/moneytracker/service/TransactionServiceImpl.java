package com.moneytracker.service.impl;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummaryDTO;
import com.moneytracker.entity.Person;
import com.moneytracker.entity.Transaction;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.exception.ResourceNotFoundException;
import com.moneytracker.exception.UnauthorizedAccessException;
import com.moneytracker.repository.PersonRepository;
import com.moneytracker.repository.TransactionRepository;
import com.moneytracker.service.PersonService;
import com.moneytracker.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TransactionService
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final PersonRepository personRepository;
    private final PersonService personService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  PersonRepository personRepository,
                                  PersonService personService) {
        this.transactionRepository = transactionRepository;
        this.personRepository = personRepository;
        this.personService = personService;
    }

    @Override
    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        logger.info("Creating new transaction for person ID: {}", transactionDTO.getPersonId());

        // Validate person exists
        Person person = personRepository.findById(transactionDTO.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID: " + transactionDTO.getPersonId()));

        Transaction transaction = convertToEntity(transactionDTO);
        transaction.setPerson(person);

        // Set transaction date to current time if not provided
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        logger.info("Successfully created transaction with ID: {}", savedTransaction.getId());
        return convertToDTO(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionDTO> getTransactionById(Long id, Long personId) {
        logger.debug("Fetching transaction by ID: {} for person: {}", id, personId);

        return transactionRepository.findByIdAndPersonId(id, personId)
                .map(this::convertToDTO);
    }

    @Override
    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO, Long personId) {
        logger.info("Updating transaction with ID: {} for person: {}", id, personId);

        Transaction existingTransaction = transactionRepository.findByIdAndPersonId(id, personId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Verify the person owns this transaction
        if (!existingTransaction.getPerson().getId().equals(personId)) {
            throw new UnauthorizedAccessException("Not authorized to update this transaction");
        }

        // Update fields
        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setDescription(transactionDTO.getDescription());
        existingTransaction.setTransactionType(transactionDTO.getTransactionType());
        existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
        existingTransaction.setCategory(transactionDTO.getCategory());
        existingTransaction.setTags(transactionDTO.getTags());
        existingTransaction.setLocation(transactionDTO.getLocation());
        existingTransaction.setNotes(transactionDTO.getNotes());

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        logger.info("Successfully updated transaction with ID: {}", id);
        return convertToDTO(updatedTransaction);
    }

    @Override
    public void deleteTransaction(Long id, Long personId) {
        logger.info("Deleting transaction with ID: {} for person: {}", id, personId);

        Transaction transaction = transactionRepository.findByIdAndPersonId(id, personId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Verify the person owns this transaction
        if (!transaction.getPerson().getId().equals(personId)) {
            throw new UnauthorizedAccessException("Not authorized to delete this transaction");
        }

        transactionRepository.delete(transaction);
        logger.info("Successfully deleted transaction with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByPersonId(Long personId, Pageable pageable) {
        logger.debug("Fetching transactions for person ID: {} with pagination: {}", personId, pageable);
        return transactionRepository.findByPersonIdOrderByTransactionDateDesc(personId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByPersonIdAndDateRange(Long personId, LocalDateTime startDate,
                                                                      LocalDateTime endDate, Pageable pageable) {
        logger.debug("Fetching transactions for person ID: {} between {} and {}", personId, startDate, endDate);
        return transactionRepository.findByPersonIdAndDateRange(personId, startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getTransactionSummary(Long personId) {
        logger.debug("Calculating transaction summary for person ID: {}", personId);

        BigDecimal totalIncome = transactionRepository.sumAmountByPersonIdAndTransactionType(personId, TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository.sumAmountByPersonIdAndTransactionType(personId, TransactionType.EXPENSE);
        long totalTransactions = transactionRepository.countByPersonId(personId);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netAmount = totalIncome.subtract(totalExpense);

        TransactionSummaryDTO summary = new TransactionSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetAmount(netAmount);
        summary.setTotalTransactions(totalTransactions);

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getTransactionSummary(Long personId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Calculating transaction summary for person ID: {} between {} and {}", personId, startDate, endDate);

        BigDecimal totalIncome = transactionRepository.sumAmountByPersonIdAndTransactionTypeAndDateRange(
                personId, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByPersonIdAndTransactionTypeAndDateRange(
                personId, TransactionType.EXPENSE, startDate, endDate);
        long totalTransactions = transactionRepository.countByPersonIdAndDateRange(personId, startDate, endDate);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netAmount = totalIncome.subtract(totalExpense);

        TransactionSummaryDTO summary = new TransactionSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetAmount(netAmount);
        summary.setTotalTransactions(totalTransactions);
        summary.setPeriodStart(startDate);
        summary.setPeriodEnd(endDate);

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctCategories(Long personId) {
        logger.debug("Fetching distinct categories for person ID: {}", personId);
        return transactionRepository.findDistinctCategoriesByPersonId(personId);
    }

    @Override
    public TransactionDTO convertToDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setCategory(transaction.getCategory());
        dto.setTags(transaction.getTags());
        dto.setLocation(transaction.getLocation());
        dto.setNotes(transaction.getNotes());
        dto.setPersonId(transaction.getPerson().getId());
        dto.setPerson(personService.convertToDTO(transaction.getPerson()));
        dto.setCreatedDate(transaction.getCreatedDate());
        dto.setLastModifiedDate(transaction.getLastModifiedDate());
        dto.setVersion(transaction.getVersion());

        return dto;
    }

    @Override
    public Transaction convertToEntity(TransactionDTO transactionDTO) {
        if (transactionDTO == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setId(transactionDTO.getId());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setTransactionType(transactionDTO.getTransactionType());
        transaction.setTransactionDate(transactionDTO.getTransactionDate());
        transaction.setCategory(transactionDTO.getCategory());
        transaction.setTags(transactionDTO.getTags());
        transaction.setLocation(transactionDTO.getLocation());
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setVersion(transactionDTO.getVersion());

        return transaction;
    }
}