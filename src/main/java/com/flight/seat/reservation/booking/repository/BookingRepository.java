package com.flight.seat.reservation.booking.repository;

import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusAndHoldExpiresAtBefore(BookingStatus status, Instant time);
}
