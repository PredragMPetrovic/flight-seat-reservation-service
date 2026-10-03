package com.flight.seat.reservation.repository;

import com.flight.seat.reservation.entity.Flight;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Override
    @EntityGraph(attributePaths = "seats")
    List<Flight> findAll();
}
