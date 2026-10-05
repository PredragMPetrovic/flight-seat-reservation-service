package com.flight.seat.reservation.admin.controller;

import com.flight.seat.reservation.admin.service.AdminService;
import com.flight.seat.reservation.flight.dto.FlightDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void createFlightReturnsCreatedWithLocation() {
        FlightDTO request = FlightDTO.builder().departureCity("Belgrade").build();
        FlightDTO created = FlightDTO.builder().id(7L).departureCity("Belgrade").build();
        when(adminService.createFlight(request)).thenReturn(created);

        ResponseEntity<FlightDTO> response = adminController.createFlight(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(created);
        assertThat(Objects.requireNonNull(response.getHeaders().getLocation()).toString()).isEqualTo("/api/v1/admin/flights/7");
    }

    @Test
    void deleteFlightReturnsNoContent() {
        ResponseEntity<Void> response = adminController.deleteFlight(7L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(adminService).deleteFlight(7L);
    }
}
