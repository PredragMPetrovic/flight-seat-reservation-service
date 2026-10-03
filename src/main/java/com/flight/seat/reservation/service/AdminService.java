package com.flight.seat.reservation.service;

import com.flight.seat.reservation.dto.FlightDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FlightService flightService;

    public void createFlight(FlightDTO flightDTO) {
        // Implement the logic to create a flight
        flightService.createFlight(flightDTO);
    }

    public void deleteFlight(String flightId) {
        // Implement the logic to delete a flight
        System.out.println("Deleting flight with ID: " + flightId);
    }
}