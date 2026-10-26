package com.routinestacker.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.routinestacker.domain.Frequency;
import com.routinestacker.domain.Habit;
import com.routinestacker.dto.CreateHabitRequest;
import com.routinestacker.service.HabitService;
import com.routinestacker.service.StreakCalculator;
import com.routinestacker.web.ConflictException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

/**
 * Concurrency checks for NFR-002 and REQ-001 (20 simultaneous creates with one name).
 * Every test starts all threads together with a latch so the races are real.
 */
class InMemoryHabitRepositoryConcurrencyTest {

    @Test
    void twentySimultaneousCreatesWithOneNameProduceExactlyOneHabit() throws Exception {
        InMemoryHabitRepository repository = new InMemoryHabitRepository();
        HabitService service = new HabitService(repository, new StreakCalculator(), Clock.systemUTC());
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            results.add(pool.submit(createAfter(start, service, "Read")));
        }
        start.countDown();

        int created = 0;
        int conflicts = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                created++;
            } else {
                conflicts++;
            }
        }
        pool.shutdown();

        assertEquals(1, created);
        assertEquals(19, conflicts);
        assertEquals(1, service.list().size());
    }

    @Test
    void simultaneousCheckInsOnOneDayRecordExactlyOne() throws Exception {
        InMemoryHabitRepository repository = new InMemoryHabitRepository();
        Habit habit = seedHabit(repository, "Stretch");
        LocalDate today = LocalDate.of(2026, 10, 26);
        ExecutorService pool = Executors.newFixedThreadPool(50);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            results.add(pool.submit(() -> {
                start.await();
                return repository.addCheckIn(habit.id(), today);
            }));
        }
        start.countDown();

        int newlyAdded = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                newlyAdded++;
            }
        }
        pool.shutdown();

        assertEquals(1, newlyAdded);
        assertEquals(1, repository.checkInDates(habit.id()).size());
    }

    @Test
    void simultaneousCheckInsOnDistinctDaysKeepEveryCheckIn() throws Exception {
        InMemoryHabitRepository repository = new InMemoryHabitRepository();
        Habit habit = seedHabit(repository, "Stretch");
        LocalDate base = LocalDate.of(2026, 10, 1);
        ExecutorService pool = Executors.newFixedThreadPool(50);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            LocalDate date = base.plusDays(i);
            results.add(pool.submit(() -> {
                start.await();
                return repository.addCheckIn(habit.id(), date);
            }));
        }
        start.countDown();

        int newlyAdded = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                newlyAdded++;
            }
        }
        pool.shutdown();

        assertEquals(50, newlyAdded);
        assertEquals(50, repository.checkInDates(habit.id()).size());
    }

    @Test
    void mixedUndoAndCheckInsLeaveConsistentState() throws Exception {
        InMemoryHabitRepository repository = new InMemoryHabitRepository();
        Habit habit = seedHabit(repository, "Stretch");
        LocalDate base = LocalDate.of(2026, 10, 1);
        ExecutorService pool = Executors.newFixedThreadPool(50);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            LocalDate date = base.plusDays(i % 10);
            boolean undo = i % 3 == 0;
            results.add(pool.submit(() -> {
                start.await();
                if (undo) {
                    repository.removeCheckIn(habit.id(), date);
                } else {
                    repository.addCheckIn(habit.id(), date);
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> result : results) {
            result.get();
        }
        pool.shutdown();

        // Each date is either present or absent, never duplicated or corrupted.
        for (LocalDate date : repository.checkInDates(habit.id())) {
            assertTrue(!date.isBefore(base) && !date.isAfter(base.plusDays(9)));
        }
        assertTrue(repository.checkInDates(habit.id()).size() <= 10);
    }

    private static Callable<Boolean> createAfter(CountDownLatch start, HabitService service, String name) {
        return () -> {
            start.await();
            try {
                service.create(new CreateHabitRequest(name, "After coffee", "DAILY"));
                return true;
            } catch (ConflictException conflict) {
                return false;
            }
        };
    }

    private static Habit seedHabit(InMemoryHabitRepository repository, String name) {
        Habit habit = new Habit("habit-1", name, "anchor", Frequency.DAILY,
                java.time.Instant.parse("2026-10-01T00:00:00Z"), repository.nextSequence());
        repository.reserveName(name.toLowerCase(java.util.Locale.ROOT), habit.id());
        repository.save(habit);
        return habit;
    }
}
