package com.temkarstudios.parkinglot.model;

import com.temkarstudios.parkinglot.enums.VehicleSize;
import com.temkarstudios.parkinglot.enums.VehicleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class Vehicle {

    @Id
    @Column(name = "license_plate")
    private String licensePlate;

    @Enumerated(EnumType.ORDINAL)
    private VehicleSize size;

    @Enumerated(EnumType.ORDINAL)
    private VehicleType type;
}