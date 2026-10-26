package com.routinestacker.repository;

import com.routinestacker.domain.Habit;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Storage boundary for habits and check-ins. The only implementation is in-memory;
 * all data is lost on restart (NFR-009).
 */
public interface HabitRepository {

    /** Returns the next creation-order sequence number. */
    long nextSequence();

    /**
     * Reserves a normalized name for a habit ID. Returns {@code false} if the name is already reserved.
     * Atomic (architecture D-4).
     */
    boolean reserveName(String normalizedName, String habitId);

    /** Stores a habit and prepares its (empty) check-in set. */
    void save(Habit habit);

    Optional<Habit> findById(String id);

    /** All habits, oldest first. */
    List<Habit> findAll();

    /** Adds a check-in. Returns {@code true} only if the date was not already checked in (architecture D-5). */
    boolean addCheckIn(String habitId, LocalDate date);

    /** Removes a check-in. Idempotent. Returns {@code true} if a check-in was removed. */
    boolean removeCheckIn(String habitId, LocalDate date);

    /** A snapshot of the habit's check-in dates. Empty if the habit is unknown. */
    Set<LocalDate> checkInDates(String habitId);
}
