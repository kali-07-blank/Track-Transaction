package com.moneytracker.controller;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummary;
import com.moneytracker.service.JwtService;
import com.moneytracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtService jwtService;

    // ===== Get All Transactions =====
    @GetMapping("/all")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.getAllTransactions(userId));
    }

    // ===== Get Summary =====
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummary> getTransactionSummary(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.getTransactionSummary(userId));
    }

    // ===== Get Transactions By Person =====
    @GetMapping("/person/{personName}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPerson(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String personName) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.getTransactionsByPerson(userId, personName));
    }

    // ===== Reverse Transaction (POST for compatibility with frontend) =====
    @PostMapping("/reverse/{transactionId}")
    public ResponseEntity<Map<String, Object>> reverseTransaction(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long transactionId) {

        Long userId;
        try {
            userId = extractUserId(authHeader);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "❌ Unauthorized: Invalid or missing token.");
            return ResponseEntity.status(401).body(errorResponse);
        }

        try {
            transactionService.reverseTransaction(userId, transactionId);

            // ✅ Fetch updated data after reversal
            List<TransactionDTO> updatedTransactions = transactionService.getAllTransactions(userId);
            TransactionSummary updatedSummary = transactionService.getTransactionSummary(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ Transaction reversed successfully!");
            response.put("transactions", updatedTransactions);
            response.put("summary", updatedSummary);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "❌ Failed to reverse transaction: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ===== Utility Method =====
    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        return jwtService.getUserIdFromToken(authHeader.substring(7));
    }
}
