package com.flight.seat.reservation.flight.controller;

import com.flight.seat.reservation.booking.dto.BookingDTO;
import com.flight.seat.reservation.flight.dto.FlightDTO;
import com.flight.seat.reservation.flight.service.FlightService;
import com.flight.seat.reservation.seat.dto.SeatDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private FlightController flightController;

    @Test
    void getFlightsReturnsOkWithList() {
        List<FlightDTO> flights = List.of(FlightDTO.builder().id(1L).build());
        when(flightService.getFlights()).thenReturn(flights);

        ResponseEntity<List<FlightDTO>> response = flightController.getFlights();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(flights);
    }

    @Test
    void reserveSeatReturnsOkWithBooking() {
        BookingDTO booking = BookingDTO.builder().id(100L).build();
        SeatDTO request = SeatDTO.builder().seatNumber("1A").passengerId("7").build();
        when(flightService.reserveSeat(1L, request)).thenReturn(booking);

        ResponseEntity<BookingDTO> response = flightController.reserveSeat(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(booking);
    }
}
