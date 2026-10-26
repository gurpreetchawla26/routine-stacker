package com.routinestacker.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Replaces the system clock with a fixed Monday, 2026-10-26 (UTC) so date-dependent tests are stable.
 */
@TestConfiguration
public class FixedClockConfig {

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-10-26T08:00:00Z"), ZoneOffset.UTC);
    }
}
