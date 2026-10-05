package com.flight.seat.reservation.flight.mapper;

import com.flight.seat.reservation.flight.dto.FlightDTO;
import com.flight.seat.reservation.flight.entity.Flight;
import com.flight.seat.reservation.seat.dto.SeatDTO;
import com.flight.seat.reservation.seat.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    FlightDTO toDTO(Flight flight);

    @Mapping(target = "passengerId", source = "booking.passenger.id")
    SeatDTO toDTO(Seat seat);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seats", ignore = true)
    Flight toEntity(FlightDTO flightDTO);
}
