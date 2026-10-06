package com.flight.seat.reservation.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class BookingWindowValidator {

    @Getter
    private final int cutoffMinutes;
    private final Clock clock;

    public BookingWindowValidator(@Value("${booking.cutoff-minutes:45}") int cutoffMinutes, Clock clock) {
        this.cutoffMinutes = cutoffMinutes;
        this.clock = clock;
    }

    public boolean isBookingTooLate(Instant departureTime) {
        return Instant.now(clock).isAfter(departureTime.minus(cutoffMinutes, ChronoUnit.MINUTES));
    }

}
