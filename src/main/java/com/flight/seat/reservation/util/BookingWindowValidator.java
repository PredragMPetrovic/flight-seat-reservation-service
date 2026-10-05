package com.flight.seat.reservation.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BookingWindowValidator {

    private final int cutoffMinutes;

    public BookingWindowValidator(@Value("${booking.cutoff-minutes:45}") int cutoffMinutes) {
        this.cutoffMinutes = cutoffMinutes;
    }

    public boolean isBookingTooLate(LocalDateTime departureTime) {
        return LocalDateTime.now().isAfter(departureTime.minusMinutes(cutoffMinutes));
    }

    public int getCutoffMinutes() {
        return cutoffMinutes;
    }
}
