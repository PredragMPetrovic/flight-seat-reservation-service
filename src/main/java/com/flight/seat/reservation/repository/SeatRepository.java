package com.flight.seat.reservation.repository;

import com.flight.seat.reservation.entity.Seat;
import com.flight.seat.reservation.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Seat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    List<Seat> findByStatus(SeatStatus status);
}
