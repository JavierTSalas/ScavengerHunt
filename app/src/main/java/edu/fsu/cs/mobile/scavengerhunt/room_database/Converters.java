package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

// https://developer.android.com/training/data-storage/room/referencing-data
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
