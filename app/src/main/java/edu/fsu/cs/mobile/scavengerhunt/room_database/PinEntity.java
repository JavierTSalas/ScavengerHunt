package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


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

    public PinEntity(long pinID, long userID, double latitude, double longitude, int color, byte[] path, boolean pickedUp, String description) {
        this.pinID = pinID;
        this.userID = userID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
        this.path = path;
        this.pickedUp = pickedUp;
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
