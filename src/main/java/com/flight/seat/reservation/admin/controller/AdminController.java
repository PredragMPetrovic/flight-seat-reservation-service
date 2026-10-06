package com.flight.seat.reservation.admin.controller;

import com.flight.seat.reservation.admin.service.AdminService;
import com.flight.seat.reservation.flight.dto.FlightDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Flight management (ADMIN)")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/flights")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a flight (auto-generates 36 seats)")
    public ResponseEntity<FlightDTO> createFlight(@Valid @RequestBody FlightDTO flightDTO) {
        FlightDTO created = adminService.createFlight(flightDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/admin/flights/" + created.getId()))
                .body(created);
    }

    @DeleteMapping("/flights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a flight")
    public ResponseEntity<Void> deleteFlight(@PathVariable("id") Long flightId) {
        adminService.deleteFlight(flightId);
        return ResponseEntity.noContent().build();
    }
}
