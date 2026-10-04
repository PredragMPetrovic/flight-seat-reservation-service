package com.flight.seat.reservation.controller;

import com.flight.seat.reservation.dto.FlightDTO;
import com.flight.seat.reservation.dto.SeatDTO;
import com.flight.seat.reservation.service.FlightService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/bookings")
    public ResponseEntity<SeatDTO> reserveSeat(@PathVariable("id") Long flightId, @RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(flightService.reserveSeat(flightId, seatDTO));
    }

}
