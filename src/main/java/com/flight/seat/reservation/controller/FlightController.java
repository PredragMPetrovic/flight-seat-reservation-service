package com.flight.seat.reservation.controller;

import com.flight.seat.reservation.dto.FlightDTO;
import com.flight.seat.reservation.service.FlightService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/flights")
public class FlightController {
    private final FlightService flightService;

    @GetMapping
    public ResponseEntity<List<FlightDTO>> getFlights() {
        return ResponseEntity.ok(flightService.getFlights());
    }
}
