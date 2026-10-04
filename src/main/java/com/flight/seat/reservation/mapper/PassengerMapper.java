package com.flight.seat.reservation.mapper;

import com.flight.seat.reservation.dto.PassengerDTO;
import com.flight.seat.reservation.entity.Passenger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    PassengerDTO toDTO(Passenger passenger);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    Passenger toEntity(PassengerDTO passengerDTO);
}
