package com.flight.seat.reservation.repository;

import com.flight.seat.reservation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
