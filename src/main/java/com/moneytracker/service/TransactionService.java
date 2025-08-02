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

public interface TransactionService {
    TransactionDTO createTransaction(TransactionDTO transactionDTO);
    TransactionDTO getTransactionById(Long id);
    List<TransactionDTO> getTransactionsByPersonId(Long personId);
    List<TransactionDTO> getTransactionsByPersonIdAndType(Long personId, TransactionType type);
    List<TransactionDTO> getTransactionsByPersonIdAndCategory(Long personId, String category);
    List<TransactionDTO> getTransactionsByDateRange(Long personId, LocalDateTime startDate, LocalDateTime endDate);
    TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO);
    void deleteTransaction(Long id);
    TransactionSummaryDTO getTransactionSummary(Long personId);
    List<String> getCategoriesByPersonId(Long personId);
}

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final PersonRepository personRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, PersonRepository personRepository) {
        this.transactionRepository = transactionRepository;
        this.personRepository = personRepository;
    }

    @Override
    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        validateTransactionData(transactionDTO);

        Person person = personRepository.findById(transactionDTO.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + transactionDTO.getPersonId()));

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setTransactionType(transactionDTO.getTransactionType());
        transaction.setCategory(transactionDTO.getCategory());
        transaction.setPerson(person);

        if (transactionDTO.getTransactionDate() != null) {
            transaction.setTransactionDate(transactionDTO.getTransactionDate());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return convertToDTO(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return convertToDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPersonId(Long personId) {
        validatePersonExists(personId);
        return transactionRepository.findByPersonIdOrderByTransactionDateDesc(personId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPersonIdAndType(Long personId, TransactionType type) {
        validatePersonExists(personId);
        return transactionRepository.findByPersonIdAndTransactionTypeOrderByTransactionDateDesc(personId, type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPersonIdAndCategory(Long personId, String category) {
        validatePersonExists(personId);
        return transactionRepository.findByPersonIdAndCategoryOrderByTransactionDateDesc(personId, category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(Long personId, LocalDateTime startDate, LocalDateTime endDate) {
        validatePersonExists(personId);
        return transactionRepository.findByPersonIdAndDateRange(personId, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        validateTransactionData(transactionDTO);

        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setDescription(transactionDTO.getDescription());
        existingTransaction.setTransactionType(transactionDTO.getTransactionType());
        existingTransaction.setCategory(transactionDTO.getCategory());

        if (transactionDTO.getTransactionDate() != null) {
            existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
        }

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        return convertToDTO(updatedTransaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getTransactionSummary(Long personId) {
        validatePersonExists(personId);

        BigDecimal totalIncome = transactionRepository.getTotalIncomeByPersonId(personId);
        BigDecimal totalExpenses = transactionRepository.getTotalExpensesByPersonId(personId);
        BigDecimal balance = transactionRepository.getBalanceByPersonId(personId);

        return new TransactionSummaryDTO(totalIncome, totalExpenses, balance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCategoriesByPersonId(Long personId) {
        validatePersonExists(personId);
        return transactionRepository.getDistinctCategoriesByPersonId(personId);
    }

    private void validatePersonExists(Long personId) {
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException("Person not found with id: " + personId);
        }
    }

    private void validateTransactionData(TransactionDTO transactionDTO) {
        if (transactionDTO.getAmount() == null || transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (transactionDTO.getDescription() == null || transactionDTO.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (transactionDTO.getTransactionType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        if (transactionDTO.getPersonId() == null) {
            throw new IllegalArgumentException("Person ID is required");
        }
    }

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