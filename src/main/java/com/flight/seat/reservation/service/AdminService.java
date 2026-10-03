package com.flight.seat.reservation.service;

import com.flight.seat.reservation.dto.FlightDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FlightService flightService;

    public FlightDTO createFlight(FlightDTO flightDTO) {
        return flightService.createFlight(flightDTO);
    }

    public void deleteFlight(Long flightId) {
        flightService.deleteFlight(flightId);
    }
}