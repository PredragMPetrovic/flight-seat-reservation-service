package com.flight.seat.reservation.seat.scheduler;

import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.booking.enums.BookingStatus;
import com.flight.seat.reservation.booking.repository.BookingRepository;
import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatScheduler {
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${booking.sweep-interval-ms:60000}")
    @Transactional
    public void releaseExpiredHolds() {
        List<Booking> expiredHolds = bookingRepository
                .findByStatusAndHoldExpiresAtBefore(BookingStatus.PENDING, Instant.now(clock));

        if (expiredHolds.isEmpty()) {
            return;
        }

        List<Seat> releasedSeats = new ArrayList<>();
        for (Booking hold : expiredHolds) {
            Seat seat = hold.getSeat();
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null);
            releasedSeats.add(seat);
        }

        try {
            seatRepository.saveAllAndFlush(releasedSeats);
            log.info("Released {} expired seat hold(s)", releasedSeats.size());
        } catch (OptimisticLockingFailureException e) {
            log.warn("Some held seats were modified concurrently; skipping this run, will retry next cycle");
        }
    }
}
