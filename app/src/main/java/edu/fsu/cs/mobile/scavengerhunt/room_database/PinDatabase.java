package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/*
    Changing theses values with drop the database!
 */
@Database(entities = {PinEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class PinDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "pins-db";

    public abstract PinsDao PinsDao();

}
