package com.routinestacker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.routinestacker.dto.CreateHabitRequest;
import com.routinestacker.dto.HabitResponse;
import com.routinestacker.repository.InMemoryHabitRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/** REQ-005 newest-first history. Past dates are seeded through the repository, since the API only accepts today. */
class CheckInServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-26T08:00:00Z"), ZoneOffset.UTC);
    private final InMemoryHabitRepository repository = new InMemoryHabitRepository();
    private final HabitService habits = new HabitService(repository, new StreakCalculator(), clock);
    private final CheckInService checkIns = new CheckInService(habits, repository, clock);

    @Test
    void historyIsNewestFirstRegardlessOfInsertionOrder() {
        String habitId = createHabit("Stretch");
        repository.addCheckIn(habitId, LocalDate.of(2026, 10, 20));
        repository.addCheckIn(habitId, LocalDate.of(2026, 10, 24));
        repository.addCheckIn(habitId, LocalDate.of(2026, 10, 22));

        assertEquals(
                List.of(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 20)),
                checkIns.history(habitId));
    }

    private String createHabit(String name) {
        HabitResponse created = habits.create(new CreateHabitRequest(name, "After coffee", "DAILY"));
        return created.id();
    }
}
