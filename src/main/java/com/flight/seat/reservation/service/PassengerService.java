package com.flight.seat.reservation.service;

import com.flight.seat.reservation.dto.PassengerDTO;
import com.flight.seat.reservation.entity.Passenger;
import com.flight.seat.reservation.mapper.PassengerMapper;
import com.flight.seat.reservation.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;

    public PassengerDTO createPassenger(PassengerDTO passengerDTO) {
        Passenger passenger = passengerMapper.toEntity(passengerDTO);
        Passenger saved = passengerRepository.save(passenger);
        log.info("Successfully created Passenger with id: {}", saved.getId());
        return passengerMapper.toDTO(saved);
    }

    public List<PassengerDTO> getPassengers() {
        return passengerRepository.findAll().stream()
                .map(passengerMapper::toDTO)
                .toList();
    }
}
