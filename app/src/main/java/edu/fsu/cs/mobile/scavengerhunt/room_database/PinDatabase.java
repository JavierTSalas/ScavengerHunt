package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/*
    Changing theses values with drop the database!
 */
@Database(entities = {PinEntity.class}, version = 1)
public abstract class PinDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "pins-db";

    public abstract PinsDao PinsDao();

}
