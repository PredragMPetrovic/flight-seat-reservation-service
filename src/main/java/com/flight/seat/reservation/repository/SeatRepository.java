package com.flight.seat.reservation.repository;

import com.flight.seat.reservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    Optional<Seat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);
}
