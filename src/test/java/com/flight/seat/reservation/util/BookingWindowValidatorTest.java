package com.flight.seat.reservation.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class BookingWindowValidatorTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private final Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final BookingWindowValidator validator = new BookingWindowValidator(45, fixedClock);

    @Test
    void getCutoffMinutesReturnsConfiguredValue() {
        assertThat(validator.getCutoffMinutes()).isEqualTo(45);
    }

    @Test
    void isBookingTooLateDepartureFarInFutureReturnsFalse() {
        assertThat(validator.isBookingTooLate(NOW.plusSeconds(5 * 3600))).isFalse();
    }

    @Test
    void isBookingTooLateJustOutsideWindowReturnsFalse() {
        // 46 minutes before departure -> still allowed
        assertThat(validator.isBookingTooLate(NOW.plusSeconds(46 * 60))).isFalse();
    }

    @Test
    void isBookingTooLateJustInsideWindowReturnsTrue() {
        // 44 minutes before departure -> too late
        assertThat(validator.isBookingTooLate(NOW.plusSeconds(44 * 60))).isTrue();
    }

    @Test
    void isBookingTooLateDepartureInPastReturnsTrue() {
        assertThat(validator.isBookingTooLate(NOW.minusSeconds(3600))).isTrue();
    }
}
