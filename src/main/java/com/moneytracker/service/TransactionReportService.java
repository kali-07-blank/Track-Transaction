package com.moneytracker.service;

import com.moneytracker.dto.TransactionDTO;
import com.moneytracker.dto.TransactionSummaryDTO;
import com.moneytracker.entity.Transaction;
import com.moneytracker.enums.TransactionType;
import com.moneytracker.repository.TransactionRepository;
import com.moneytracker.util.DateRangeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionReportService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Map<String, BigDecimal> getCategoryWiseExpenses(Long personId, int year, int month) {
        LocalDateTime startDate = DateRangeUtil.getStartOfMonth(year, month);
        LocalDateTime endDate = DateRangeUtil.getEndOfMonth(year, month);

        List<Transaction> transactions = transactionRepository.findByPersonIdAndDateRange(personId, startDate, endDate);

        return transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    public Map<String, BigDecimal> getMonthlyIncome(Long personId, int year) {
        Map<String, BigDecimal> monthlyIncome = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime startDate = DateRangeUtil.getStartOfMonth(year, month);
            LocalDateTime endDate = DateRangeUtil.getEndOfMonth(year, month);

            List<Transaction> transactions = transactionRepository.findByPersonIdAndDateRange(personId, startDate, endDate);

            BigDecimal totalIncome = transactions.stream()
                    .filter(t -> t.getTransactionType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyIncome.put(String.format("%04d-%02d", year, month), totalIncome);
        }

        return monthlyIncome;
    }

    public TransactionSummaryDTO getYearlySummary(Long personId, int year) {
        LocalDateTime startDate = DateRangeUtil.getStartOfYear(year);
        LocalDateTime endDate = DateRangeUtil.getEndOfYear(year);

        List<Transaction> transactions = transactionRepository.findByPersonIdAndDateRange(personId, startDate, endDate);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        return new TransactionSummaryDTO(totalIncome, totalExpenses, balance);
    }
}