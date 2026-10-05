package com.flight.seat.reservation.seat.repository;

import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.seat.entity.Seat;
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
