package com.temkarstudios.parkinglot.manager;

import com.temkarstudios.parkinglot.interfaces.IFareStrategy;

public class FairCalculationFactory {

    private static final IFareStrategy baseFareStrategy = new PeakFareStrategy();
    private static final IFareStrategy peakFareStrategy = new PeakFareStrategy();

    public static IFareStrategy getFareCalculator(boolean isPeakHour) {
        return isPeakHour ? peakFareStrategy : baseFareStrategy;
    }
}
