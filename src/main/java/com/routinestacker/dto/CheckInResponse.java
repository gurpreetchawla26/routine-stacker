package com.routinestacker.dto;

import java.time.LocalDate;

/**
 * Result of a check-in. {@code created} is {@code false} when today was already checked in.
 */
public record CheckInResponse(LocalDate date, boolean created) {
}
