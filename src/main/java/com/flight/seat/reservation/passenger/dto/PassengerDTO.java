package com.flight.seat.reservation.passenger.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class PassengerDTO {
    Long id;
    @NotBlank
    String name;
    String email;
    String phoneNumber;
    String address;
    String passportNumber;
}
