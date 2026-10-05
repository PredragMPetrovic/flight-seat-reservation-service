package com.flight.seat.reservation.seat.scheduler;

import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatScheduler {
    private final SeatRepository seatRepository;

    @Scheduled(fixedDelay = 15 * 60 * 1000)
    @Transactional
    public void releaseReservedSeats() {
        List<Seat> reservedSeats = seatRepository.findByStatus(SeatStatus.RESERVED);

        if (reservedSeats.isEmpty()) {
            return;
        }

        for (Seat seat : reservedSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null);
        }

        try {
            seatRepository.saveAll(reservedSeats);
            log.info("Released {} reserved seat(s)", reservedSeats.size());
        } catch (OptimisticLockingFailureException e) {
            log.warn("Some reserved seats were modified concurrently; skipping this run, will retry next cycle");
        }
    }
}
