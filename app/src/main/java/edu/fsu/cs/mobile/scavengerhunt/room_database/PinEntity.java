package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;


/**
 * If you add or remove columns modify
 *
 * @PinDatabase.java version
 */
@Entity(tableName = "pins", indices = @Index("pinID"))
public class PinEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pinID")
    private long pinID;

    @ColumnInfo(name = "userID")
    private long userID;

    @ColumnInfo(name = "longitude")
    private long longitude;

    @ColumnInfo(name = "latitude")
    private long latitude;

    @ColumnInfo(name = "color")
    private int color;

    //https://stackoverflow.com/questions/9357668/how-to-store-image-in-sqlite-database
    @ColumnInfo(name = "imagePath")
    private byte[] path;

    @ColumnInfo(name = "description")
    private String description;

    public PinEntity(long pinID, long userID, long longitude, long latitude, int color, byte[] path, String description) {
        this.pinID = pinID;
        this.userID = userID;
        this.longitude = longitude;
        this.latitude = latitude;
        this.color = color;
        this.path = path;
        this.description = description;
    }

    @Override
    public String toString() {
        return "PinEntity{" +
                "pinID=" + pinID +
                ", userID=" + userID +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", color=" + color +
                ", path=" + path +
                ", description='" + description + '\'' +
                '}';
    }

    public long getPinID() {
        return pinID;
    }

    public long getUserID() {
        return userID;
    }

    public long getLongitude() {
        return longitude;
    }

    public long getLatitude() {
        return latitude;
    }

    public int getColor() {
        return color;
    }

    public byte[] getPath() {
        return  path;
    }

    public String getDescription() {
        return description;
    }
}
