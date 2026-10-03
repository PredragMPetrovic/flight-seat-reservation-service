package com.flight.seat.reservation.repository;

import com.flight.seat.reservation.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {
}
