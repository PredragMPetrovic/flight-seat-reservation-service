package com.flight.seat.reservation.passenger.controller;

import com.flight.seat.reservation.passenger.dto.PassengerDTO;
import com.flight.seat.reservation.passenger.service.PassengerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassengerControllerTest {

    @Mock
    private PassengerService passengerService;

    @InjectMocks
    private PassengerController passengerController;

    @Test
    void createPassengerReturnsCreatedWithLocation() {
        PassengerDTO request = PassengerDTO.builder().name("Pedja").build();
        PassengerDTO created = PassengerDTO.builder().id(1L).name("Pedja").build();
        when(passengerService.createPassenger(request)).thenReturn(created);

        ResponseEntity<PassengerDTO> response = passengerController.createPassenger(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(created);
        assertThat(Objects.requireNonNull(response.getHeaders().getLocation()).toString()).isEqualTo("/api/v1/passengers/1");
    }

    @Test
    void getPassengersReturnsOkWithList() {
        List<PassengerDTO> passengers = List.of(PassengerDTO.builder().id(1L).build());
        when(passengerService.getPassengers()).thenReturn(passengers);

        ResponseEntity<List<PassengerDTO>> response = passengerController.getPassengers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(passengers);
    }
}
