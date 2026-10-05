package com.flight.seat.reservation.dto;

import com.flight.seat.reservation.enums.BookingStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class BookingDTO {
    Long id;
    Long flightId;
    Long seatId;
    Long passengerId;
    BookingStatus bookingStatus;
}
