package com.flight.seat.reservation.flight.controller;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.flight.dto.FlightDTO;
import com.flight.seat.reservation.flight.service.FlightService;
import com.flight.seat.reservation.seat.dto.SeatDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/flights")
@Tag(name = "Flights", description = "Browse flights and reserve seats")
public class FlightController {
    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "List flights, optionally filtered by departure date (UTC) and/or route (departure/destination city)")
    public ResponseEntity<List<FlightDTO>> getFlights(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String departureCity,
            @RequestParam(required = false) String destinationCity) {
        return ResponseEntity.ok(flightService.getFlights(date, departureCity, destinationCity));
    }

    @PostMapping("/{id}/bookings")
    @Operation(summary = "Reserve a seat on a flight (creates a PENDING booking)")
    public ResponseEntity<BookingDTO> reserveSeat(@PathVariable("id") Long flightId, @RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(flightService.reserveSeat(flightId, seatDTO));
    }

}
