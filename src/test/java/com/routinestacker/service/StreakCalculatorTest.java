package com.routinestacker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.routinestacker.domain.Frequency;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** REQ-006 streak scenarios, using fixed dates. Monday 2026-10-26 is "today". */
class StreakCalculatorTest {

    private final StreakCalculator calculator = new StreakCalculator();
    private final LocalDate today = LocalDate.of(2026, 10, 26);

    @Test
    void dailyStreakIncludingToday() {
        Set<LocalDate> dates = Set.of(today, today.minusDays(1), today.minusDays(2));
        assertEquals(3, calculator.calculate(dates, Frequency.DAILY, today));
    }

    @Test
    void dailyStreakThatHasNotYetBeenCheckedInToday() {
        Set<LocalDate> dates = Set.of(today.minusDays(1), today.minusDays(2));
        assertEquals(2, calculator.calculate(dates, Frequency.DAILY, today));
    }

    @Test
    void dailyStreakBrokenByAMissedDay() {
        Set<LocalDate> dates = Set.of(today.minusDays(2), today);
        assertEquals(1, calculator.calculate(dates, Frequency.DAILY, today));
    }

    @Test
    void dailyStreakIsZeroWithNoCheckIns() {
        assertEquals(0, calculator.calculate(Set.of(), Frequency.DAILY, today));
    }

    @Test
    void weeklyStreakCountsConsecutiveWeeksIncludingThisWeek() {
        Set<LocalDate> dates = Set.of(today, today.minusWeeks(1), today.minusWeeks(2));
        assertEquals(3, calculator.calculate(dates, Frequency.WEEKLY, today));
    }

    @Test
    void weeklyStreakContinuesFromLastWeekWhenThisWeekIsEmpty() {
        Set<LocalDate> dates = Set.of(today.minusWeeks(1), today.minusWeeks(2));
        assertEquals(2, calculator.calculate(dates, Frequency.WEEKLY, today));
    }

    @Test
    void weeklyWeeksStartOnMonday() {
        // Sunday 2026-10-25 belongs to the week that starts Monday 2026-10-19, so it is last week's check-in.
        LocalDate sundayBefore = today.minusDays(1);
        assertEquals(1, calculator.calculate(Set.of(sundayBefore), Frequency.WEEKLY, today));
    }

    @Test
    void weeklyStreakBrokenByAMissedWeek() {
        Set<LocalDate> dates = Set.of(today, today.minusWeeks(2));
        assertEquals(1, calculator.calculate(dates, Frequency.WEEKLY, today));
    }
}
