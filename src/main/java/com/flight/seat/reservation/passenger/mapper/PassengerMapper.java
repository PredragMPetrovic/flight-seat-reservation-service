package com.flight.seat.reservation.passenger.mapper;

import com.flight.seat.reservation.passenger.dto.PassengerDTO;
import com.flight.seat.reservation.passenger.entity.Passenger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    PassengerDTO toDTO(Passenger passenger);

    @Mapping(target = "id", ignore = true)
    Passenger toEntity(PassengerDTO passengerDTO);
}
