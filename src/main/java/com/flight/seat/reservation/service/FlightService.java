package com.flight.seat.reservation.service;

import com.flight.seat.reservation.dto.FlightDTO;
import com.flight.seat.reservation.entity.Flight;
import com.flight.seat.reservation.entity.Seat;
import com.flight.seat.reservation.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

        Flight saved = flightRepository.save(flight);

        log.info("Successfully created Flight with id: {}", saved.getId());
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
                    .build();
            seats.add(seat);
        }
        return seats;
    }

    public List<FlightDTO> getFlights() {
        List<Flight> flights = flightRepository.findAll();
        List<FlightDTO> flightDTOs = new ArrayList<>();

        for (Flight flight : flights) {
            FlightDTO flightDTO = FlightDTO.builder()
                    .departureCity(flight.getDepartureCity())
                    .departureAirport(flight.getDepartureAirport())
                    .destinationCity(flight.getDestinationCity())
                    .destinationAirport(flight.getDestinationAirport())
                    .departureDateTime(flight.getDepartureDateTime())
                    .seats(flight.getSeats().stream()
                            .map(Seat::toDTO)
                            .toList())
                    .build();
            flightDTOs.add(flightDTO);
        }

        return flightDTOs;
    }

}
