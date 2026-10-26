package com.routinestacker.service;

import com.routinestacker.domain.Frequency;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Pure streak logic (REQ-006). Weeks are ISO weeks starting on Monday (OQ-3).
 */
@Component
public class StreakCalculator {

    public int calculate(Set<LocalDate> checkInDates, Frequency frequency, LocalDate today) {
        return frequency == Frequency.WEEKLY
                ? weeklyStreak(checkInDates, today)
                : dailyStreak(checkInDates, today);
    }

    private int dailyStreak(Set<LocalDate> dates, LocalDate today) {
        // Today counts if checked in; otherwise the streak may still continue from yesterday.
        LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private int weeklyStreak(Set<LocalDate> dates, LocalDate today) {
        Set<LocalDate> weeks = new HashSet<>();
        for (LocalDate date : dates) {
            weeks.add(mondayOf(date));
        }
        LocalDate thisWeek = mondayOf(today);
        LocalDate cursor = weeks.contains(thisWeek) ? thisWeek : thisWeek.minusWeeks(1);
        int streak = 0;
        while (weeks.contains(cursor)) {
            streak++;
            cursor = cursor.minusWeeks(1);
        }
        return streak;
    }

    private static LocalDate mondayOf(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
