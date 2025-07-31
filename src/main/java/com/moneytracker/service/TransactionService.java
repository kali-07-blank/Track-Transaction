package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummary;
import com.moneytracker.entity.Transaction;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<TransactionDTO> getAllTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionSummary getTransactionSummary(Long userId) {
        Double totalSent = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.SEND);
        Double totalReceived = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.RECEIVE);

        totalSent = totalSent != null ? totalSent : 0.0;
        totalReceived = totalReceived != null ? totalReceived : 0.0;

        return new TransactionSummary(totalSent, totalReceived, totalReceived - totalSent);
    }

    public List<TransactionDTO> getTransactionsByPerson(Long userId, String personName) {
        return transactionRepository.findByUserIdAndPersonName(userId, personName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        PersonDTO personDTO = new PersonDTO(
                transaction.getPerson().getId(),
                transaction.getPerson().getName(),
                transaction.getPerson().getBalance(),
                transaction.getPerson().getCreatedAt()
        );

        return new TransactionDTO(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                personDTO
        );
    }
}
