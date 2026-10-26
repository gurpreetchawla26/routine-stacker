package com.routinestacker.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.routinestacker.domain.Habit;
import com.routinestacker.dto.CreateHabitRequest;
import com.routinestacker.dto.HabitResponse;
import com.routinestacker.repository.InMemoryHabitRepository;
import com.routinestacker.service.HabitService;
import com.routinestacker.service.StreakCalculator;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * NFR-001 timing check: p95 at or below 500 ms for a single operation at 1,000 habits and 100,000 check-ins.
 * Measured in-process against the service layer with plain JUnit and {@code System.nanoTime()}; no benchmarking
 * tools (implementation plan OQ-P2). Results depend on machine load, so the check is a guard, not a benchmark.
 */
class LatencyTest {

    private static final int HABITS = 1_000;
    private static final int CHECK_INS_PER_HABIT = 100;
    private static final long P95_LIMIT_NANOS = 500_000_000L;

    @Test
    void p95LatencyStaysWithinLimitAtNfr001Load() {
        InMemoryHabitRepository repository = new InMemoryHabitRepository();
        HabitService service = new HabitService(repository, new StreakCalculator(), Clock.systemUTC());

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < HABITS; i++) {
            HabitResponse habit = service.create(new CreateHabitRequest("Habit " + i, "anchor", "DAILY"));
            ids.add(habit.id());
        }
        LocalDate base = LocalDate.of(2020, 1, 1);
        for (String id : ids) {
            for (int day = 0; day < CHECK_INS_PER_HABIT; day++) {
                repository.addCheckIn(id, base.plusDays(day));
            }
        }

        // Warm up so JIT compilation does not dominate the first samples.
        for (int i = 0; i < 50; i++) {
            service.get(ids.get(i % ids.size()));
        }

        long[] samples = new long[200];
        for (int i = 0; i < samples.length; i++) {
            String id = ids.get(i % ids.size());
            long start = System.nanoTime();
            service.get(id);
            samples[i] = System.nanoTime() - start;
        }

        long p95 = p95(samples);
        assertTrue(p95 <= P95_LIMIT_NANOS,
                "p95 get latency was " + (p95 / 1_000_000) + " ms, limit is 500 ms");
    }

    private static long p95(long[] samples) {
        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        int index = (int) Math.ceil(0.95 * sorted.length) - 1;
        return sorted[index];
    }
}
