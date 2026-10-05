package com.flight.seat.reservation.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingWindowValidatorTest {

    private final BookingWindowValidator validator = new BookingWindowValidator(45);

    @Test
    void getCutoffMinutesReturnsConfiguredValue() {
        assertThat(validator.getCutoffMinutes()).isEqualTo(45);
    }

    @Test
    void isBookingTooLateDepartureFarInFutureReturnsFalse() {
        assertThat(validator.isBookingTooLate(LocalDateTime.now().plusHours(5))).isFalse();
    }

    @Test
    void isBookingTooLateWithinCutoffWindowReturnsTrue() {
        assertThat(validator.isBookingTooLate(LocalDateTime.now().plusMinutes(10))).isTrue();
    }

    @Test
    void isBookingTooLateDepartureInPastReturnsTrue() {
        assertThat(validator.isBookingTooLate(LocalDateTime.now().minusHours(1))).isTrue();
    }
}
