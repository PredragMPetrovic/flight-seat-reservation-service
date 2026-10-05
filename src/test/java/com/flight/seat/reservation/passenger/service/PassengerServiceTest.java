package com.flight.seat.reservation.passenger.service;

import com.flight.seat.reservation.passenger.dto.PassengerDTO;
import com.flight.seat.reservation.passenger.entity.Passenger;
import com.flight.seat.reservation.passenger.mapper.PassengerMapper;
import com.flight.seat.reservation.passenger.repository.PassengerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private PassengerMapper passengerMapper;

    @InjectMocks
    private PassengerService passengerService;

    @Test
    void createPassengerSavesAndReturnsDto() {
        PassengerDTO request = PassengerDTO.builder().name("Pedja").build();
        Passenger entity = Passenger.builder().name("Pedja").build();
        Passenger saved = Passenger.builder().id(1L).name("Pedja").build();
        PassengerDTO expected = PassengerDTO.builder().id(1L).name("Pedja").build();

        when(passengerMapper.toEntity(request)).thenReturn(entity);
        when(passengerRepository.save(entity)).thenReturn(saved);
        when(passengerMapper.toDTO(saved)).thenReturn(expected);

        assertThat(passengerService.createPassenger(request)).isSameAs(expected);
    }

    @Test
    void getPassengersMapsAll() {
        Passenger p1 = Passenger.builder().id(1L).build();
        Passenger p2 = Passenger.builder().id(2L).build();
        PassengerDTO d1 = PassengerDTO.builder().id(1L).build();
        PassengerDTO d2 = PassengerDTO.builder().id(2L).build();
        when(passengerRepository.findAll()).thenReturn(List.of(p1, p2));
        when(passengerMapper.toDTO(p1)).thenReturn(d1);
        when(passengerMapper.toDTO(p2)).thenReturn(d2);

        assertThat(passengerService.getPassengers()).containsExactly(d1, d2);
    }
}
