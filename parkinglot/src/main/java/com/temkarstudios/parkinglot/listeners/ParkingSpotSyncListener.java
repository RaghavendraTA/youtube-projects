package com.temkarstudios.parkinglot.listeners;

import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.services.AsyncDatabaseUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Event listener to sync parking spot changes to cache
 * Ensures consistency between database and Redis cache
 */
@Component
public class ParkingSpotSyncListener {

    @Autowired
    private AsyncDatabaseUpdateService asyncDatabaseUpdateService;

    /**
     * Called after parking spot is saved to database
     * Syncs the change to Redis cache asynchronously
     */
    public void onParkingSpotUpdate(ParkingSpot spot) {
        asyncDatabaseUpdateService.syncCacheWithDatabase(spot.getId(), spot);
    }
}
