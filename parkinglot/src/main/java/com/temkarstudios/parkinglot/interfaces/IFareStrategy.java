package com.temkarstudios.parkinglot.interfaces;

import com.temkarstudios.parkinglot.model.Ticket;

public interface IFareStrategy {

    float CalculateFare(Ticket ticket);
}
