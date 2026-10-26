package com.routinestacker.web;

import java.time.Instant;

/**
 * The one error shape used for every 4xx and 5xx response (NFR-004).
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
