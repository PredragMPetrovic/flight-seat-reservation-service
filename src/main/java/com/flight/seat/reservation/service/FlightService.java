package com.flight.seat.reservation.service;

import com.flight.seat.reservation.dto.FlightDTO;
import com.flight.seat.reservation.entity.Flight;
import com.flight.seat.reservation.entity.Seat;
import com.flight.seat.reservation.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

    private static final String[] seatNumbers = {
            "1A", "1B", "1C", "1D", "1E", "1F",
            "2A", "2B", "2C", "2D", "2E", "2F",
            "3A", "3B", "3C", "3D", "3E", "3F",
            "4A", "4B", "4C", "4D", "4E", "4F",
            "5A", "5B", "5C", "5D", "5E", "5F",
            "6A", "6B", "6C", "6D", "6E", "6F"
    };

    public void createFlight(FlightDTO flightDTO) {
        Flight flight = Flight.builder()
                .departureCity(flightDTO.getDepartureCity())
                .departureAirport(flightDTO.getDepartureAirport())
                .destinationCity(flightDTO.getDestinationCity())
                .destinationAirport(flightDTO.getDestinationAirport())
                .departureDateTime(flightDTO.getDepartureDateTime())
                .build();

        List<Seat> seats = createSeats(flight);
        flight.setSeats(seats);

        flightRepository.save(flight);
    }

    private List<Seat> createSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();
        for (String seatNumber : seatNumbers) {
            Seat seat = Seat.builder()
                    .flight(flight)
                    .seatNumber(seatNumber)
                    .build();
            seats.add(seat);
        }
        return seats;
    }
}
