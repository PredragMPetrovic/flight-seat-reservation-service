package com.flight.seat.reservation.booking.service;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.booking.enums.BookingStatus;
import com.flight.seat.reservation.booking.mapper.BookingMapper;
import com.flight.seat.reservation.booking.repository.BookingRepository;
import com.flight.seat.reservation.flight.entity.Flight;
import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.exception.BookingWindowException;
import com.flight.seat.reservation.exception.NotFoundException;
import com.flight.seat.reservation.exception.SessionExpiredException;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import com.flight.seat.reservation.util.BookingWindowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final BookingMapper bookingMapper;
    private final BookingWindowValidator bookingWindowValidator;
    private final Clock clock;

    @Transactional
    public BookingDTO confirmReservation(Long reservationId) {

        Optional<Booking> booking = bookingRepository.findById(reservationId);
        if (booking.isEmpty()) {
            throw new SessionExpiredException("Your session has expired. Please try booking again.");
        }

        Instant holdExpiresAt = booking.get().getHoldExpiresAt();
        if (holdExpiresAt != null && holdExpiresAt.isBefore(Instant.now(clock))) {
            log.warn("Reservation {} hold expired at {}", reservationId, holdExpiresAt);
            throw new SessionExpiredException("Your reservation has expired. Please try booking again.");
        }

        Flight flight = booking.get().getSeat().getFlight();
        if(bookingWindowValidator.isBookingTooLate(flight.getDepartureDateTime())) {
            log.warn("Booking is closed for flight with ID {}. Attempted to book a seat at {}.",
                    flight.getId(), flight.getDepartureDateTime());
            throw new BookingWindowException("Booking is closed for this flight. You can only book a seat up to "
                    + bookingWindowValidator.getCutoffMinutes() + " minutes before departure.");
        }

        Seat seat = booking.get().getSeat();
        if (seat.getStatus() != SeatStatus.RESERVED) {
            throw new SessionExpiredException("Your reservation is no longer valid. Please try booking again.");
        }

        seat.setStatus(SeatStatus.OCCUPIED);
        booking.get().setStatus(BookingStatus.CONFIRMED);
        seat.setBooking(booking.get());

        Seat savedSeat;
        try {
            savedSeat = seatRepository.saveAndFlush(seat);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Reservation {} was modified concurrently during confirmation", reservationId);
            throw new SessionExpiredException("Your reservation is no longer valid. Please try booking again.");
        }

        log.info("Reservation confirmed successfully.");

        return bookingMapper.toDTO(savedSeat.getBooking());
    }

    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(bookingMapper::toDTO)
                .toList();
    }

    public void deleteReservation(Long reservationId) {
        Optional<Booking> booking = bookingRepository.findById(reservationId);
        if (booking.isEmpty()) {
            throw new NotFoundException("Booking with ID " + reservationId + " not found.");
        }

        Seat seat = booking.get().getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setBooking(null);
        seatRepository.save(seat);

        log.info("Deleted reservation with ID {}", reservationId);
    }

}
