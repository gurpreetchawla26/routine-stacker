package com.routinestacker.dto;

import com.routinestacker.domain.Frequency;
import java.time.Instant;

/**
 * Habit representation returned by every habit endpoint (architecture D-12).
 */
public record HabitResponse(
        String id,
        String name,
        String anchorRoutine,
        Frequency frequency,
        Instant createdAt,
        int streak,
        boolean checkedInToday) {
}
