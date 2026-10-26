package com.routinestacker.service;

import com.routinestacker.dto.CheckInResponse;
import com.routinestacker.repository.HabitRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Check-in rules: one check-in per day, same-day undo, and newest-first history (REQ-003 to REQ-005).
 * Unknown habits are rejected with 404 (architecture OQ-A1).
 */
@Service
public class CheckInService {

    private final HabitService habits;
    private final HabitRepository repository;
    private final Clock clock;

    public CheckInService(HabitService habits, HabitRepository repository, Clock clock) {
        this.habits = habits;
        this.repository = repository;
        this.clock = clock;
    }

    public CheckInResponse checkIn(String habitId) {
        habits.requireHabit(habitId);
        LocalDate today = LocalDate.now(clock);
        boolean created = repository.addCheckIn(habitId, today);
        return new CheckInResponse(today, created);
    }

    public void undoToday(String habitId) {
        habits.requireHabit(habitId);
        repository.removeCheckIn(habitId, LocalDate.now(clock));
    }

    public List<LocalDate> history(String habitId) {
        habits.requireHabit(habitId);
        return repository.checkInDates(habitId).stream()
                .sorted(Comparator.reverseOrder())
                .toList();
    }
}
