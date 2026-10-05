package com.flight.seat.reservation.admin.service;

import com.flight.seat.reservation.flight.dto.FlightDTO;
import com.flight.seat.reservation.flight.service.FlightService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void createFlightDelegatesToFlightService() {
        FlightDTO request = FlightDTO.builder().departureCity("Belgrade").build();
        FlightDTO expected = FlightDTO.builder().id(1L).departureCity("Belgrade").build();
        when(flightService.createFlight(request)).thenReturn(expected);

        assertThat(adminService.createFlight(request)).isSameAs(expected);
    }

    @Test
    void deleteFlightDelegatesToFlightService() {
        adminService.deleteFlight(9L);
        verify(flightService).deleteFlight(9L);
    }
}
