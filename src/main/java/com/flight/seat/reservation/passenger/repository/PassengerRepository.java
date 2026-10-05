package com.flight.seat.reservation.passenger.repository;

import com.flight.seat.reservation.passenger.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}
