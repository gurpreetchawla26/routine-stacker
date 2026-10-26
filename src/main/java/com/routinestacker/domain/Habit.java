package com.routinestacker.domain;

import java.time.Instant;

/**
 * An immutable habit. {@code sequence} gives creation order and is not exposed over the API.
 */
public record Habit(
        String id,
        String name,
        String anchorRoutine,
        Frequency frequency,
        Instant createdAt,
        long sequence) {
}
