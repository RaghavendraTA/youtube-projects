package com.temkarstudios.parkinglot.manager;

import com.temkarstudios.parkinglot.interfaces.IFareStrategy;
import com.temkarstudios.parkinglot.model.Ticket;

public class BaseFareStrategy implements IFareStrategy {

    @Override
    public float CalculateFare(Ticket ticket) {
        var diffTime = ticket.getExitTime().getTime() - ticket.getEntryTime().getTime();

        float absDiff = Math.abs(diffTime);
        var totalMinutes = Math.floor(absDiff / (1000 * 60));
        var hours = Math.floor(totalMinutes / 60);
        var minutes = totalMinutes % 60;

        float totalPrice = (float) (hours * 2 * ticket.getParkingSpot().getPrice());

        return totalPrice + (minutes > 30 ? 2 : 1) * ticket.getParkingSpot().getPrice();
    }
}
