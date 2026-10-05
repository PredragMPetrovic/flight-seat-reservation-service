package com.flight.seat.reservation.admin.service;

import com.flight.seat.reservation.flight.dto.FlightDTO;
import com.flight.seat.reservation.flight.service.FlightService;
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