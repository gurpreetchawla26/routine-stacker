package com.routinestacker.repository;

import com.routinestacker.domain.Habit;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * Thread-safe in-memory storage built on concurrent collections. Each operation is atomic on a single
 * map entry; habit creation is the only operation that writes to more than one map (architecture section 4.3).
 */
@Repository
public class InMemoryHabitRepository implements HabitRepository {

    private final ConcurrentHashMap<String, Habit> habits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> nameIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<LocalDate>> checkIns = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public long nextSequence() {
        return sequence.incrementAndGet();
    }

    @Override
    public boolean reserveName(String normalizedName, String habitId) {
        return nameIndex.putIfAbsent(normalizedName, habitId) == null;
    }

    @Override
    public void save(Habit habit) {
        // The check-in set is created before the habit becomes visible, so a visible habit always has one.
        checkIns.put(habit.id(), ConcurrentHashMap.newKeySet());
        habits.put(habit.id(), habit);
    }

    @Override
    public Optional<Habit> findById(String id) {
        return Optional.ofNullable(habits.get(id));
    }

    @Override
    public List<Habit> findAll() {
        return habits.values().stream()
                .sorted(Comparator.comparingLong(Habit::sequence))
                .toList();
    }

    @Override
    public boolean addCheckIn(String habitId, LocalDate date) {
        Set<LocalDate> dates = checkIns.get(habitId);
        return dates != null && dates.add(date);
    }

    @Override
    public boolean removeCheckIn(String habitId, LocalDate date) {
        Set<LocalDate> dates = checkIns.get(habitId);
        return dates != null && dates.remove(date);
    }

    @Override
    public Set<LocalDate> checkInDates(String habitId) {
        Set<LocalDate> dates = checkIns.get(habitId);
        return dates == null ? Set.of() : Set.copyOf(dates);
    }
}
