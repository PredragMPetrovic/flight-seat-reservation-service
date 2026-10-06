package com.flight.seat.reservation.booking.controller;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Confirm, list and cancel reservations")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Confirm a reservation (marks the seat OCCUPIED)")
    public ResponseEntity<BookingDTO> confirmReservation(@PathVariable("id") Long reservationId) {
        BookingDTO created = bookingService.confirmReservation(reservationId);
        return ResponseEntity
                .created(URI.create("/api/v1/bookings/" + created.getId()))
                .body(created);
    }

    @GetMapping
    @Operation(summary = "List all bookings")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Cancel a reservation (releases the seat)")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long reservationId) {
        bookingService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

}
