package com.temkarstudios.parkinglot.dto;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotRequest {

    private Long spotId;
    private float price;
    private float peakPrice;
    private ParkingSpotType spotType;
}
