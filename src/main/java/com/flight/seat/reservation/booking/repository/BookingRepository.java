package com.flight.seat.reservation.booking.repository;

import com.flight.seat.reservation.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
