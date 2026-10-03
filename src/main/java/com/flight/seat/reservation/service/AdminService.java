package com.flight.seat.reservation.service;

import com.flight.seat.reservation.dto.FlightDTO;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    public void createFlight(FlightDTO flightDTO) {
        // Implement the logic to create a flight
        System.out.println(flightDTO);
    }

    public void deleteFlight(String flightId) {
        // Implement the logic to delete a flight
        System.out.println("Deleting flight with ID: " + flightId);
    }
}