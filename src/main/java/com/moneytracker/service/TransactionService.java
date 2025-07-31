package com.moneytracker.service;

import com.moneytracker.dto.PersonDTO;
import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummary;
import com.moneytracker.entity.Person;
import com.moneytracker.entity.Transaction;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.repository.PersonRepository;
import com.moneytracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PersonRepository personRepository;

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

    public void reverseTransaction(Long userId, Long transactionId) {
        Optional<Transaction> optTx = transactionRepository.findById(transactionId);

        if (optTx.isEmpty()) {
            throw new RuntimeException("Transaction not found");
        }

        Transaction original = optTx.get();

        if (!original.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to reverse this transaction");
        }

        Person person = original.getPerson();
        double amount = original.getAmount();

        // Create opposite type transaction
        TransactionType reversedType =
                original.getType() == TransactionType.SEND ? TransactionType.RECEIVE : TransactionType.SEND;

        Transaction reversed = new Transaction();
        reversed.setUser(original.getUser());
        reversed.setPerson(person);
        reversed.setAmount(amount);
        reversed.setDescription("Reversal of Tx #" + original.getId());
        reversed.setType(reversedType);
        reversed.setTransactionDate(LocalDateTime.now());

        // Adjust balance
        if (reversedType == TransactionType.SEND) {
            person.setBalance(person.getBalance() + amount);
        } else {
            person.setBalance(person.getBalance() - amount);
        }

        transactionRepository.save(reversed);
        personRepository.save(person);
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
