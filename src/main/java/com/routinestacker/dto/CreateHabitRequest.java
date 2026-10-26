package com.routinestacker.dto;

/**
 * Request body for {@code POST /api/habits}. Fields are validated in {@code HabitService}, so every
 * rule returns the same error shape (NFR-003, NFR-004). {@code frequency} is a string so that an unknown
 * value gets the same field-level message as a missing one.
 */
public record CreateHabitRequest(String name, String anchorRoutine, String frequency) {
}
