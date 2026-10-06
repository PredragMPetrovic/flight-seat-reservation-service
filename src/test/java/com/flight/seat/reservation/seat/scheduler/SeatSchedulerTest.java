package com.flight.seat.reservation.seat.scheduler;

import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.booking.enums.BookingStatus;
import com.flight.seat.reservation.booking.repository.BookingRepository;
import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatSchedulerTest {

    private static final Instant NOW = Instant.parse("2026-10-06T10:00:00Z");

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatRepository seatRepository;

    private SeatScheduler seatScheduler;

    @BeforeEach
    void setUp() {
        seatScheduler = new SeatScheduler(bookingRepository, seatRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private Seat heldSeat(long id) {
        Seat seat = Seat.builder().id(id).status(SeatStatus.RESERVED).build();
        Booking booking = Booking.builder().id(id).seat(seat).status(BookingStatus.PENDING).build();
        seat.setBooking(booking);
        return seat;
    }

    @Test
    void releaseExpiredHoldsReleasesOnlyExpiredHeldSeats() {
        Seat s1 = heldSeat(1L);
        Seat s2 = heldSeat(2L);
        when(bookingRepository.findByStatusAndHoldExpiresAtBefore(eq(BookingStatus.PENDING), eq(NOW)))
                .thenReturn(List.of(s1.getBooking(), s2.getBooking()));

        seatScheduler.releaseExpiredHolds();

        assertThat(s1.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(s1.getBooking()).isNull();
        assertThat(s2.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(s2.getBooking()).isNull();
        verify(seatRepository).saveAllAndFlush(List.of(s1, s2));
    }

    @Test
    void releaseExpiredHoldsNoExpiredHoldsDoesNothing() {
        when(bookingRepository.findByStatusAndHoldExpiresAtBefore(eq(BookingStatus.PENDING), eq(NOW)))
                .thenReturn(List.of());

        seatScheduler.releaseExpiredHolds();

        verify(seatRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void releaseExpiredHoldsOptimisticLockFailureIsSwallowed() {
        Seat s1 = heldSeat(1L);
        when(bookingRepository.findByStatusAndHoldExpiresAtBefore(eq(BookingStatus.PENDING), eq(NOW)))
                .thenReturn(List.of(s1.getBooking()));
        when(seatRepository.saveAllAndFlush(anyList())).thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThatCode(() -> seatScheduler.releaseExpiredHolds()).doesNotThrowAnyException();
    }
}
