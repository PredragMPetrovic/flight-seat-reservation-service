package com.flight.seat.reservation.booking.controller;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void confirmReservationReturnsCreatedWithLocation() {
        BookingDTO booking = BookingDTO.builder().id(100L).build();
        when(bookingService.confirmReservation(100L)).thenReturn(booking);

        ResponseEntity<BookingDTO> response = bookingController.confirmReservation(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(booking);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().toString()).isEqualTo("/api/v1/bookings/100");
    }

    @Test
    void getAllBookingsReturnsOkWithList() {
        List<BookingDTO> bookings = List.of(BookingDTO.builder().id(1L).build());
        when(bookingService.getAllBookings()).thenReturn(bookings);

        ResponseEntity<List<BookingDTO>> response = bookingController.getAllBookings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(bookings);
    }

    @Test
    void deleteReservationReturnsNoContent() {
        ResponseEntity<Void> response = bookingController.deleteReservation(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(bookingService).deleteReservation(100L);
    }
}
