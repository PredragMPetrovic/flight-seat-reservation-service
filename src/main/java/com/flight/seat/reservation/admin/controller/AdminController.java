package com.flight.seat.reservation.admin.controller;

import com.flight.seat.reservation.admin.service.AdminService;
import com.flight.seat.reservation.flight.dto.FlightDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/flights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightDTO> createFlight(@RequestBody FlightDTO flightDTO) {
        FlightDTO created = adminService.createFlight(flightDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/admin/flights/" + created.getId()))
                .body(created);
    }

    @DeleteMapping("/flights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFlight(@PathVariable("id") Long flightId) {
        adminService.deleteFlight(flightId);
        return ResponseEntity.noContent().build();
    }
}
