package com.flight.seat.reservation.passenger.controller;

import com.flight.seat.reservation.passenger.dto.PassengerDTO;
import com.flight.seat.reservation.passenger.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/passengers")
@Tag(name = "Passengers", description = "Manage passengers")
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping
    @Operation(summary = "Create a passenger")
    public ResponseEntity<PassengerDTO> createPassenger(@Valid @RequestBody PassengerDTO passengerDTO) {
        PassengerDTO created = passengerService.createPassenger(passengerDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/passengers/" + created.getId()))
                .body(created);
    }

    @GetMapping
    @Operation(summary = "List all passengers")
    public ResponseEntity<List<PassengerDTO>> getPassengers() {
        return ResponseEntity.ok(passengerService.getPassengers());
    }
}
