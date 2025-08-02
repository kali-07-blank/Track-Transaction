package com.moneytracker.controller;

import com.moneytracker.dto.*;
import com.moneytracker.entity.Person;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for transaction management operations
 *
 * @author MoneyTracker Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transaction Management", description = "Transaction management APIs")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody TransactionDTO transactionDTO) {

        Long currentUserId = getCurrentUserId();
        transactionDTO.setPersonId(currentUserId);

        logger.info("Create transaction request for user: {}", currentUserId);

        TransactionDTO createdTransaction = transactionService.createTransaction(transactionDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdTransaction, "Transaction created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();

        logger.debug("Get transaction request for ID: {} by user: {}", id, currentUserId);

        return transactionService.getTransactionById(id, currentUserId)
                .map(transaction -> ResponseEntity.ok(ApiResponse.success(transaction)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO transactionDTO) {

        Long currentUserId = getCurrentUserId();

        logger.info("Update transaction request for ID: {} by user: {}", id, currentUserId);

        TransactionDTO updatedTransaction = transactionService.updateTransaction(id, transactionDTO, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(updatedTransaction, "Transaction updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction")
    public ResponseEntity<ApiResponse<String>> deleteTransaction(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();

        logger.info("Delete transaction request for ID: {} by user: {}", id, currentUserId);

        transactionService.deleteTransaction(id, currentUserId);

        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Get user's transactions")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionDTO>>> getTransactions(
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DDTHH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DDTHH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @Parameter(description = "Transaction type") TransactionType type,
            @RequestParam(required = false) @Parameter(description = "Category") String category) {

        Long currentUserId = getCurrentUserId();

        logger.debug("Get transactions request for user: {} with filters", currentUserId);

        Page<TransactionDTO> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionService.getTransactionsByPersonIdAndDateRange(
                    currentUserId, startDate, endDate, pageable);
        } else {
            transactions = transactionService.getTransactionsByPersonId(currentUserId, pageable);
        }

        PagedResponse<TransactionDTO> response = PagedResponse.of(transactions);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get transaction summary")
    public ResponseEntity<ApiResponse<TransactionSummaryDTO>> getTransactionSummary(
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DDTHH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DDTHH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Long currentUserId = getCurrentUserId();

        logger.debug("Get transaction summary request for user: {}", currentUserId);

        TransactionSummaryDTO summary;

        if (startDate != null && endDate != null) {
            summary = transactionService.getTransactionSummary(currentUserId, startDate, endDate);
        } else {
            summary = transactionService.getTransactionSummary(currentUserId);
        }

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get distinct categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        Long currentUserId = getCurrentUserId();

        logger.debug("Get categories request for user: {}", currentUserId);

        List<String> categories = transactionService.getDistinctCategories(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Person person = (Person) auth.getPrincipal();
        return person.getId();
    }
}