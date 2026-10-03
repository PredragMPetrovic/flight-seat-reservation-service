package com.flight.seat.reservation.controller;

import com.flight.seat.reservation.dto.FlightDTO;
import com.flight.seat.reservation.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/flights")
    @PreAuthorize("hasRole('ADMIN')")
    public void createFlight(@RequestBody FlightDTO flightDTO) {
        adminService.createFlight(flightDTO);
    }

    @DeleteMapping("/flights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFlight(@PathVariable("id") String flightId) {
        adminService.deleteFlight(Long.valueOf(flightId));
    }
}
