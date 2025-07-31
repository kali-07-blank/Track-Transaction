package com.moneytracker.controller;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummary;
import com.moneytracker.service.JwtService;
import com.moneytracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/all")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.getAllTransactions(userId));
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummary> getTransactionSummary(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.getTransactionSummary(userId));
    }

    @GetMapping("/person/{personName}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPerson(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String personName) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.getTransactionsByPerson(userId, personName));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        return jwtService.getUserIdFromToken(authHeader.substring(7));
    }
}
