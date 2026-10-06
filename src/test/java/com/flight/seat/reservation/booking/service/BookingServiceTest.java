package com.flight.seat.reservation.booking.service;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.booking.enums.BookingStatus;
import com.flight.seat.reservation.booking.mapper.BookingMapper;
import com.flight.seat.reservation.booking.repository.BookingRepository;
import com.flight.seat.reservation.exception.BookingWindowException;
import com.flight.seat.reservation.exception.NotFoundException;
import com.flight.seat.reservation.exception.SessionExpiredException;
import com.flight.seat.reservation.flight.entity.Flight;
import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import com.flight.seat.reservation.util.BookingWindowValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingWindowValidator bookingWindowValidator;

    @InjectMocks
    private BookingService bookingService;

    private Booking reservedBooking(Instant departure, SeatStatus seatStatus) {
        Flight flight = Flight.builder().id(1L).departureDateTime(departure).build();
        Seat seat = Seat.builder().id(10L).seatNumber("1A").status(seatStatus).flight(flight).build();
        Booking booking = Booking.builder().id(100L).seat(seat).status(BookingStatus.PENDING).build();
        seat.setBooking(booking);
        return booking;
    }

    @Test
    void confirmReservationSuccessMarksOccupiedAndConfirmed() {
        Booking booking = reservedBooking(Instant.now().plusSeconds(5 * 3600), SeatStatus.RESERVED);
        BookingDTO expected = BookingDTO.builder().id(100L).build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.saveAndFlush(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(expected);

        BookingDTO result = bookingService.confirmReservation(100L);

        assertThat(result).isSameAs(expected);
        assertThat(booking.getSeat().getStatus()).isEqualTo(SeatStatus.OCCUPIED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmReservationNotFoundThrowsSessionExpired() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmReservation(100L))
                .isInstanceOf(SessionExpiredException.class);
    }

    @Test
    void confirmReservationBookingTooLateThrowsBookingWindow() {
        Booking booking = reservedBooking(Instant.now().plusSeconds(10 * 60), SeatStatus.RESERVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(true);
        when(bookingWindowValidator.getCutoffMinutes()).thenReturn(45);

        assertThatThrownBy(() -> bookingService.confirmReservation(100L))
                .isInstanceOf(BookingWindowException.class);
    }

    @Test
    void confirmReservationSeatNotReservedThrowsSessionExpired() {
        Booking booking = reservedBooking(Instant.now().plusSeconds(5 * 3600), SeatStatus.AVAILABLE);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.confirmReservation(100L))
                .isInstanceOf(SessionExpiredException.class);
    }

    @Test
    void confirmReservationOptimisticLockThrowsSessionExpired() {
        Booking booking = reservedBooking(Instant.now().plusSeconds(5 * 3600), SeatStatus.RESERVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.saveAndFlush(any(Seat.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThatThrownBy(() -> bookingService.confirmReservation(100L))
                .isInstanceOf(SessionExpiredException.class);
    }

    @Test
    void getAllBookingsMapsAll() {
        Booking b1 = Booking.builder().id(1L).build();
        Booking b2 = Booking.builder().id(2L).build();
        BookingDTO d1 = BookingDTO.builder().id(1L).build();
        BookingDTO d2 = BookingDTO.builder().id(2L).build();
        when(bookingRepository.findAll()).thenReturn(List.of(b1, b2));
        when(bookingMapper.toDTO(b1)).thenReturn(d1);
        when(bookingMapper.toDTO(b2)).thenReturn(d2);

        assertThat(bookingService.getAllBookings()).containsExactly(d1, d2);
    }

    @Test
    void deleteReservationSuccessReleasesSeat() {
        Booking booking = reservedBooking(Instant.now().plusSeconds(5 * 3600), SeatStatus.RESERVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        bookingService.deleteReservation(100L);

        assertThat(booking.getSeat().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(booking.getSeat().getBooking()).isNull();
        verify(seatRepository).save(booking.getSeat());
    }

    @Test
    void deleteReservationNotFoundThrowsNotFound() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteReservation(100L))
                .isInstanceOf(NotFoundException.class);
        verify(seatRepository, never()).save(any());
    }
}
