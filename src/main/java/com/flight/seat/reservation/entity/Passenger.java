package com.flight.seat.reservation.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "passenger")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String passportNumber;

    @OneToOne(mappedBy = "passenger", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Booking booking;
}
