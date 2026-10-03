package com.flight.seat.reservation.repository;

import com.flight.seat.reservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
