package com.moneytracker.util;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

public class DateRangeUtil {

    public static LocalDateTime getStartOfMonth(int year, int month) {
        return YearMonth.of(year, month).atDay(1).atStartOfDay();
    }

    public static LocalDateTime getEndOfMonth(int year, int month) {
        return YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59, 999_999_999);
    }

    public static LocalDateTime getStartOfYear(int year) {
        return LocalDateTime.of(year, 1, 1, 0, 0, 0, 0);
    }

    public static LocalDateTime getEndOfYear(int year) {
        return LocalDateTime.of(year, 12, 31, 23, 59, 59, 999_999_999);
    }

    public static LocalDateTime getStartOfWeek(LocalDateTime date) {
        return date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static LocalDateTime getEndOfWeek(LocalDateTime date) {
        return date.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
    }
}
