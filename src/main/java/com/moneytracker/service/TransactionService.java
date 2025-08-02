package com.moneytracker.service;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummaryDTO;
import com.moneytracker.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

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
