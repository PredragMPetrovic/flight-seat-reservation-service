package com.flight.seat.reservation.booking.controller;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.booking.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<BookingDTO> confirmReservation(@PathVariable("id") Long reservationId) {
        BookingDTO created = bookingService.confirmReservation(reservationId);
        return ResponseEntity
                .created(URI.create("/api/v1/bookings/" + created.getId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long reservationId) {
        bookingService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

}
