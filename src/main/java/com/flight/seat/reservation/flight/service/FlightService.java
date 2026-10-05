package com.flight.seat.reservation.flight.service;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.flight.dto.FlightDTO;
import com.flight.seat.reservation.flight.entity.Flight;
import com.flight.seat.reservation.flight.mapper.FlightMapper;
import com.flight.seat.reservation.flight.repository.FlightRepository;
import com.flight.seat.reservation.seat.dto.SeatDTO;
import com.flight.seat.reservation.booking.entity.Booking;
import com.flight.seat.reservation.passenger.entity.Passenger;
import com.flight.seat.reservation.seat.entity.Seat;
import com.flight.seat.reservation.booking.enums.BookingStatus;
import com.flight.seat.reservation.seat.enums.SeatStatus;
import com.flight.seat.reservation.booking.mapper.BookingMapper;
import com.flight.seat.reservation.passenger.repository.PassengerRepository;
import com.flight.seat.reservation.seat.repository.SeatRepository;
import com.flight.seat.reservation.util.BookingWindowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final PassengerRepository passengerRepository;
    private final FlightMapper flightMapper;
    private final BookingMapper bookingMapper;
    private final BookingWindowValidator bookingWindowValidator;

    private static final String[] seatNumbers = {
            "1A", "1B", "1C", "1D", "1E", "1F",
            "2A", "2B", "2C", "2D", "2E", "2F",
            "3A", "3B", "3C", "3D", "3E", "3F",
            "4A", "4B", "4C", "4D", "4E", "4F",
            "5A", "5B", "5C", "5D", "5E", "5F",
            "6A", "6B", "6C", "6D", "6E", "6F"
    };

    public FlightDTO createFlight(FlightDTO flightDTO) {
        Flight flight = flightMapper.toEntity(flightDTO);

        List<Seat> seats = createSeats(flight);
        flight.setSeats(seats);

        Flight saved = flightRepository.save(flight);

        log.info("Successfully created Flight with id: {}", saved.getId());
        return flightMapper.toDTO(saved);
    }

    public void deleteFlight(Long flightId) {
        flightRepository.deleteById(flightId);
        log.info("Successfully deleted Flight with id: {}", flightId);
    }

    private List<Seat> createSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();
        for (String seatNumber : seatNumbers) {
            Seat seat = Seat.builder()
                    .flight(flight)
                    .seatNumber(seatNumber)
                    .status(SeatStatus.AVAILABLE)
                    .build();
            seats.add(seat);
        }
        return seats;
    }

    public List<FlightDTO> getFlights() {
        return flightRepository.findAll().stream()
                .map(flightMapper::toDTO)
                .toList();
    }

    @Transactional
    public BookingDTO reserveSeat(Long flightId, SeatDTO seatDTO) {
        if (isBookingTooLateForFlight(flightId)) {
            log.warn("Booking is closed for flight {}. Cannot reserve seat {} for passenger {}",
                    flightId, seatDTO.getSeatNumber(), seatDTO.getPassengerId());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Booking is closed for this flight. You can only book a seat up to "
                            + bookingWindowValidator.getCutoffMinutes() + " minutes before departure.");
        }

        Seat seat = seatRepository
                .findByFlightIdAndSeatNumber(flightId, seatDTO.getSeatNumber())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Seat %s not found on flight %d".formatted(seatDTO.getSeatNumber(), flightId)));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat %s on flight %d is not available".formatted(seatDTO.getSeatNumber(), flightId));
        }

        Passenger passenger = getPassenger(seatDTO.getPassengerId());

        Booking booking = Booking.builder()
                .seat(seat)
                .passenger(passenger)
                .status(BookingStatus.PENDING)
                .build();
        
        seat.setBooking(booking);
        seat.setStatus(SeatStatus.RESERVED);

        Seat saved;
        try {
            saved = seatRepository.saveAndFlush(seat);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent booking conflict for seat {} on flight {}",
                    seatDTO.getSeatNumber(), flightId);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat %s on flight %d is no longer available".formatted(seatDTO.getSeatNumber(), flightId));
        }

        log.info("Reserved seat {} on flight {} for passenger {}",
                saved.getSeatNumber(), flightId, passenger.getId());

        return bookingMapper.toDTO(saved.getBooking());
    }

    private boolean isBookingTooLateForFlight(Long flightId) {
        Optional<Flight> flight = flightRepository.findById(flightId);
        if (flight.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Flight %d not found".formatted(flightId));
        }

        return bookingWindowValidator.isBookingTooLate(flight.get().getDepartureDateTime());
    }

    private Passenger getPassenger(String passengerId) {
        if (passengerId == null || passengerId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passengerId is required");
        }

        long id;
        try {
            id = Long.parseLong(passengerId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "passengerId must be a number: " + passengerId);
        }

        return passengerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Passenger %d not found".formatted(id)));
    }
}
