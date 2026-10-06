package com.flight.seat.reservation.flight.service;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.booking.mapper.BookingMapper;
import com.flight.seat.reservation.exception.BookingWindowException;
import com.flight.seat.reservation.exception.InvalidRequestException;
import com.flight.seat.reservation.exception.NotFoundException;
import com.flight.seat.reservation.exception.SeatUnavailableException;
import com.flight.seat.reservation.flight.dto.FlightDTO;
import com.flight.seat.reservation.flight.entity.Flight;
import com.flight.seat.reservation.flight.mapper.FlightMapper;
import com.flight.seat.reservation.flight.repository.FlightRepository;
import com.flight.seat.reservation.passenger.entity.Passenger;
import com.flight.seat.reservation.passenger.repository.PassengerRepository;
import com.flight.seat.reservation.seat.dto.SeatDTO;
import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import com.flight.seat.reservation.util.BookingWindowValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private FlightMapper flightMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingWindowValidator bookingWindowValidator;

    @InjectMocks
    private FlightService flightService;

    private SeatDTO reserveRequest() {
        return SeatDTO.builder().seatNumber("1A").passengerId("7").build();
    }

    private Flight flightDepartingAt(Instant departure) {
        return Flight.builder().id(1L).departureDateTime(departure).build();
    }

    @Test
    void createFlightGeneratesSeatsAndReturnsDto() {
        FlightDTO request = FlightDTO.builder().departureCity("Belgrade").build();
        Flight entity = Flight.builder().id(1L).departureCity("Belgrade").build();
        FlightDTO expected = FlightDTO.builder().id(1L).departureCity("Belgrade").build();

        when(flightMapper.toEntity(request)).thenReturn(entity);
        when(flightRepository.save(any(Flight.class))).thenReturn(entity);
        when(flightMapper.toDTO(entity)).thenReturn(expected);

        FlightDTO result = flightService.createFlight(request);

        assertThat(result).isSameAs(expected);

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(captor.capture());
        List<Seat> seats = captor.getValue().getSeats();
        assertThat(seats).hasSize(36);
        assertThat(seats).allMatch(s -> s.getStatus() == SeatStatus.AVAILABLE);
        assertThat(seats).allMatch(s -> s.getFlight() == entity);
    }

    @Test
    void getFlightsMapsAllFlights() {
        Flight f1 = Flight.builder().id(1L).build();
        Flight f2 = Flight.builder().id(2L).build();
        FlightDTO d1 = FlightDTO.builder().id(1L).build();
        FlightDTO d2 = FlightDTO.builder().id(2L).build();
        when(flightRepository.findAll()).thenReturn(List.of(f1, f2));
        when(flightMapper.toDTO(f1)).thenReturn(d1);
        when(flightMapper.toDTO(f2)).thenReturn(d2);

        assertThat(flightService.getFlights()).containsExactly(d1, d2);
    }

    @Test
    void deleteFlightDelegatesToRepository() {
        flightService.deleteFlight(5L);
        verify(flightRepository).deleteById(5L);
    }

    @Test
    void reserveSeatSuccessReturnsBooking() {
        SeatDTO request = reserveRequest();
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(5 * 3600));
        Seat seat = Seat.builder().id(10L).seatNumber("1A").status(SeatStatus.AVAILABLE).flight(flight).build();
        Passenger passenger = Passenger.builder().id(7L).build();
        BookingDTO expected = BookingDTO.builder().id(100L).build();

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(flight.getDepartureDateTime())).thenReturn(false);
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.of(seat));
        when(passengerRepository.findById(7L)).thenReturn(Optional.of(passenger));
        when(seatRepository.saveAndFlush(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(expected);

        BookingDTO result = flightService.reserveSeat(1L, request);

        assertThat(result).isSameAs(expected);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(seat.getBooking()).isNotNull();
        assertThat(seat.getBooking().getPassenger()).isSameAs(passenger);
    }

    @Test
    void reserveSeatBookingTooLateThrowsBookingWindow() {
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(10 * 60));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(flight.getDepartureDateTime())).thenReturn(true);
        when(bookingWindowValidator.getCutoffMinutes()).thenReturn(45);

        assertThatThrownBy(() -> flightService.reserveSeat(1L, reserveRequest()))
                .isInstanceOf(BookingWindowException.class);
    }

    @Test
    void reserveSeatFlightNotFoundThrowsNotFound() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.reserveSeat(1L, reserveRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void reserveSeatSeatNotFoundThrowsNotFound() {
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(5 * 3600));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.reserveSeat(1L, reserveRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void reserveSeatSeatNotAvailableThrowsSeatUnavailable() {
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(5 * 3600));
        Seat seat = Seat.builder().seatNumber("1A").status(SeatStatus.RESERVED).flight(flight).build();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> flightService.reserveSeat(1L, reserveRequest()))
                .isInstanceOf(SeatUnavailableException.class);
    }

    @Test
    void reserveSeatMissingPassengerIdThrowsInvalidRequest() {
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(5 * 3600));
        Seat seat = Seat.builder().seatNumber("1A").status(SeatStatus.AVAILABLE).flight(flight).build();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.of(seat));

        SeatDTO request = SeatDTO.builder().seatNumber("1A").passengerId(null).build();

        assertThatThrownBy(() -> flightService.reserveSeat(1L, request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void reserveSeatNonNumericPassengerIdThrowsInvalidRequest() {
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(5 * 3600));
        Seat seat = Seat.builder().seatNumber("1A").status(SeatStatus.AVAILABLE).flight(flight).build();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.of(seat));

        SeatDTO request = SeatDTO.builder().seatNumber("1A").passengerId("abc").build();

        assertThatThrownBy(() -> flightService.reserveSeat(1L, request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void reserveSeatPassengerNotFoundThrowsNotFound() {
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(5 * 3600));
        Seat seat = Seat.builder().seatNumber("1A").status(SeatStatus.AVAILABLE).flight(flight).build();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.of(seat));
        when(passengerRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.reserveSeat(1L, reserveRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void reserveSeatDataIntegrityViolationThrowsSeatUnavailable() {
        Flight flight = flightDepartingAt(Instant.now().plusSeconds(5 * 3600));
        Seat seat = Seat.builder().seatNumber("1A").status(SeatStatus.AVAILABLE).flight(flight).build();
        Passenger passenger = Passenger.builder().id(7L).build();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(bookingWindowValidator.isBookingTooLate(any())).thenReturn(false);
        when(seatRepository.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.of(seat));
        when(passengerRepository.findById(7L)).thenReturn(Optional.of(passenger));
        when(seatRepository.saveAndFlush(any(Seat.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> flightService.reserveSeat(1L, reserveRequest()))
                .isInstanceOf(SeatUnavailableException.class);
    }
}
