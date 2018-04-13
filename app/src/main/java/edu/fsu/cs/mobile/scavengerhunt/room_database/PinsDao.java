package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PinsDao {
    @Query("SELECT * FROM pins")
        // DO NOT USE THIS - I'M NOT EVEN SURE WHY I'M WRITING ONE
    List<PinEntity> getAllPins();

    @Query("SELECT * FROM pins WHERE pinID = :pinID")
    PinEntity getSingleEntity(long pinID);

    @Query("SELECT Count(*) FROM pins")
    int getCountOfEntities();

    // Use this one
    @Query("SELECT * FROM pins WHERE " +
            "(latitude between :latMin AND :latMax)" +
            "AND" +
            "(longitude between :lonMin AND :lonMax)")
    List<PinEntity> getPinsInsideBoundingBox(long latMin, long latMax , long lonMin, long lonMax);

    @Query("SELECT pinID FROM pins ORDER BY pinID DESC LIMIT 1")
    long getPinIDOfLastEntry();

    @Query("SELECT * FROM pins WHERE " +
            "(latitude = :lat)" +
            "AND" +
            "(longitude = :lon)")
    List<PinEntity> getPinFromLatLon(long lat, long lon);

    //Will be used in PinDatabaseCreator.java to call populateData()
    @Insert
    void insertAll(PinEntity... pinEntities);

    @Insert
    void insert(PinEntity pinEntity);

    @Delete
    void delete(PinEntity pinEntity);

}
