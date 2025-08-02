package com.moneytracker.controller;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummaryDTO;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO createdTransaction = transactionService.createTransaction(transactionDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPersonId(@PathVariable Long personId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByPersonId(personId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/person/{personId}/type/{type}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPersonIdAndType(
            @PathVariable Long personId,
            @PathVariable TransactionType type) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByPersonIdAndType(personId, type);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/person/{personId}/category/{category}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPersonIdAndCategory(
            @PathVariable Long personId,
            @PathVariable String category) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByPersonIdAndCategory(personId, category);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/person/{personId}/date-range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @PathVariable Long personId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(personId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/person/{personId}/summary")
    public ResponseEntity<TransactionSummaryDTO> getTransactionSummary(@PathVariable Long personId) {
        TransactionSummaryDTO summary = transactionService.getTransactionSummary(personId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/person/{personId}/categories")
    public ResponseEntity<List<String>> getCategoriesByPersonId(@PathVariable Long personId) {
        List<String> categories = transactionService.getCategoriesByPersonId(personId);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO updatedTransaction = transactionService.updateTransaction(id, transactionDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}