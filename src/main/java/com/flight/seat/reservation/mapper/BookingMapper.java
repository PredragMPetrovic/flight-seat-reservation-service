package com.flight.seat.reservation.mapper;

import com.flight.seat.reservation.dto.BookingDTO;
import com.flight.seat.reservation.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "flightId", source = "seat.flight.id")
    @Mapping(target = "seatNumber", source = "seat.seatNumber")
    @Mapping(target = "passengerId", source = "passenger.id")
    @Mapping(target = "bookingStatus", source = "status")
    BookingDTO toDTO(Booking booking);
}
