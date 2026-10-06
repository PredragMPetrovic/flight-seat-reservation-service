package com.flight.seat.reservation.flight.repository;

import com.flight.seat.reservation.flight.entity.Flight;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @EntityGraph(attributePaths = {"seats", "seats.booking", "seats.booking.passenger"})
    @Query("""
            SELECT f FROM Flight f
            WHERE (:departureCity IS NULL OR LOWER(f.departureCity) = LOWER(:departureCity))
              AND (:destinationCity IS NULL OR LOWER(f.destinationCity) = LOWER(:destinationCity))
              AND (:from IS NULL OR f.departureDateTime >= :from)
              AND (:to IS NULL OR f.departureDateTime < :to)
            """)
    List<Flight> findFiltered(@Param("departureCity") String departureCity,
                              @Param("destinationCity") String destinationCity,
                              @Param("from") Instant from,
                              @Param("to") Instant to);
}
