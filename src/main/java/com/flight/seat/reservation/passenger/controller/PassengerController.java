package com.flight.seat.reservation.passenger.controller;

import com.flight.seat.reservation.passenger.dto.PassengerDTO;
import com.flight.seat.reservation.passenger.service.PassengerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/passengers")
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<PassengerDTO> createPassenger(@RequestBody PassengerDTO passengerDTO) {
        PassengerDTO created = passengerService.createPassenger(passengerDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/passengers/" + created.getId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<PassengerDTO>> getPassengers() {
        return ResponseEntity.ok(passengerService.getPassengers());
    }
}
