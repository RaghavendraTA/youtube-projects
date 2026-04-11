package com.temkarstudios.parkinglot.dto;

import com.temkarstudios.parkinglot.enums.VehicleType;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OccupiedVehicleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spotId;
    private VehicleType carType;
    private Date entryTime;
}
