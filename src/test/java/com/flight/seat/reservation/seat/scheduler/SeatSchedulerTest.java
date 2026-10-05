package com.flight.seat.reservation.seat.scheduler;

import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatSchedulerTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatScheduler seatScheduler;

    @Test
    void releaseReservedSeatsReleasesAllReservedSeats() {
        Seat s1 = Seat.builder().id(1L).status(SeatStatus.RESERVED).booking(Booking.builder().id(1L).build()).build();
        Seat s2 = Seat.builder().id(2L).status(SeatStatus.RESERVED).booking(Booking.builder().id(2L).build()).build();
        when(seatRepository.findByStatus(SeatStatus.RESERVED)).thenReturn(List.of(s1, s2));

        seatScheduler.releaseReservedSeats();

        assertThat(s1.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(s1.getBooking()).isNull();
        assertThat(s2.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(s2.getBooking()).isNull();
        verify(seatRepository).saveAll(List.of(s1, s2));
    }

    @Test
    void releaseReservedSeatsNoReservedSeatsDoesNothing() {
        when(seatRepository.findByStatus(SeatStatus.RESERVED)).thenReturn(List.of());

        seatScheduler.releaseReservedSeats();

        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    void releaseReservedSeatsOptimisticLockFailureIsSwallowed() {
        Seat s1 = Seat.builder().id(1L).status(SeatStatus.RESERVED).build();
        when(seatRepository.findByStatus(SeatStatus.RESERVED)).thenReturn(List.of(s1));
        when(seatRepository.saveAll(anyList())).thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThatCode(() -> seatScheduler.releaseReservedSeats()).doesNotThrowAnyException();
    }
}
