package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Date;


/**
 * If you add or remove columns modify
 *
 * @PinDatabase.java version
 */
@Entity(tableName = "pins", indices = @Index("pinID"))
public class PinEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "pinID")
    private String pinID;

    @ColumnInfo(name = "userID")
    private String userID;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "color")
    private int color;

    //https://stackoverflow.com/questions/9357668/how-to-store-image-in-sqlite-database
    @ColumnInfo(name = "imageBLOB")
    private byte[] path;

    @ColumnInfo(name = "hasBeenPickedUp")
    private boolean pickedUp;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "timePlaced")
    private Date timePlaced;

    public PinEntity(String pinID, String userID, double longitude, double latitude, int color, byte[] path, boolean pickedUp, String description, Date timePlaced) {
        this.pinID = pinID;
        this.userID = userID;
        this.longitude = longitude;
        this.latitude = latitude;
        this.color = color;
        this.path = path;
        this.pickedUp = pickedUp;
        this.description = description;
        this.timePlaced = timePlaced;
    }

    public Date getTimePlaced() {

        return timePlaced;
    }

    @Override
    public String toString() {
        return "PinEntity{" +
                "pinID=" + pinID +
                ", userID=" + userID +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", color=" + color +
                ", path=" + Arrays.toString(path) +
                ", pickedUp=" + pickedUp +
                ", description='" + description + '\'' +
                ", timePlaced=" + timePlaced +
                '}';
    }

    public String getPinID() {
        return pinID;
    }

    public String getUserID() {
        return userID;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getColor() {
        return color;
    }

    public byte[] getPath() {
        return  path;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public String getDescription() {
        return description;
    }


}
