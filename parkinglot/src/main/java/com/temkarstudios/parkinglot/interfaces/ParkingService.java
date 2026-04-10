package com.temkarstudios.parkinglot.interfaces;

import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.model.Ticket;

public interface ParkingService {

    Ticket enterVehicle(Request request) throws Exception;

    Ticket exitVehicle(Long ticketId) throws Exception;
}
