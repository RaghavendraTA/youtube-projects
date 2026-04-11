package com.temkarstudios.parkinglot.model;

import jakarta.persistence.*;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "license_plate_no", referencedColumnName = "license_plate")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "parking_spot_no", referencedColumnName = "id")
    private ParkingSpot parkingSpot;

    private Date entryTime;
    private Date exitTime;

    private boolean isActive;
    private float finalPrice;
}