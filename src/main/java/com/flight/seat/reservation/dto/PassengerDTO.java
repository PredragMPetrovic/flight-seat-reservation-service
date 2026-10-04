package com.flight.seat.reservation.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class PassengerDTO {
    Long id;
    String name;
    String email;
    String phoneNumber;
    String address;
    String passportNumber;
}
