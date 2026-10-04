package com.flight.seat.reservation.scheduler;

import com.flight.seat.reservation.entity.Seat;
import com.flight.seat.reservation.enums.SeatStatus;
import com.flight.seat.reservation.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        }

        seatRepository.saveAll(reservedSeats);
        log.info("Released {} reserved seat(s)", reservedSeats.size());
    }
}
