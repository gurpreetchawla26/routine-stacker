package com.routinestacker.service;

import com.routinestacker.domain.Frequency;
import com.routinestacker.domain.Habit;
import com.routinestacker.dto.CreateHabitRequest;
import com.routinestacker.dto.HabitResponse;
import com.routinestacker.repository.HabitRepository;
import com.routinestacker.web.ConflictException;
import com.routinestacker.web.NotFoundException;
import com.routinestacker.web.ValidationException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Habit rules: validation, uniqueness, listing, and response assembly (REQ-001, REQ-002, REQ-006).
 */
@Service
public class HabitService {

    static final int NAME_MAX_LENGTH = 100;
    static final int ANCHOR_MAX_LENGTH = 150;

    private final HabitRepository repository;
    private final StreakCalculator streaks;
    private final Clock clock;

    public HabitService(HabitRepository repository, StreakCalculator streaks, Clock clock) {
        this.repository = repository;
        this.streaks = streaks;
        this.clock = clock;
    }

    public HabitResponse create(CreateHabitRequest request) {
        String name = requireText("name", request.name(), NAME_MAX_LENGTH);
        String anchorRoutine = requireText("anchorRoutine", request.anchorRoutine(), ANCHOR_MAX_LENGTH);
        Frequency frequency = parseFrequency(request.frequency());

        String id = UUID.randomUUID().toString();
        // Reserve the name first so two concurrent creates cannot both succeed (architecture D-4).
        if (!repository.reserveName(normalizeName(name), id)) {
            throw new ConflictException("A habit named \"" + name + "\" already exists");
        }
        Habit habit = new Habit(id, name, anchorRoutine, frequency, Instant.now(clock), repository.nextSequence());
        repository.save(habit);
        return toResponse(habit);
    }

    public List<HabitResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public HabitResponse get(String id) {
        return toResponse(requireHabit(id));
    }

    /** Returns the habit or throws {@link NotFoundException}. Shared with the check-in rules. */
    public Habit requireHabit(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Habit not found: " + id));
    }

    private HabitResponse toResponse(Habit habit) {
        Set<LocalDate> dates = repository.checkInDates(habit.id());
        LocalDate today = LocalDate.now(clock);
        return new HabitResponse(
                habit.id(),
                habit.name(),
                habit.anchorRoutine(),
                habit.frequency(),
                habit.createdAt(),
                streaks.calculate(dates, habit.frequency(), today),
                dates.contains(today));
    }

    private static String requireText(String field, String value, int maxLength) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(field + ": must not be blank");
        }
        if (trimmed.length() > maxLength) {
            throw new ValidationException(field + ": must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    private static Frequency parseFrequency(String value) {
        if (value != null) {
            for (Frequency frequency : Frequency.values()) {
                if (frequency.name().equals(value)) {
                    return frequency;
                }
            }
        }
        throw new ValidationException("frequency: must be one of DAILY, WEEKLY");
    }

    private static String normalizeName(String trimmedName) {
        return trimmedName.toLowerCase(Locale.ROOT);
    }
}
