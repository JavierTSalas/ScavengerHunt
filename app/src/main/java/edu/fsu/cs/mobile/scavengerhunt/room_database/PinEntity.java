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

    /*
        This should work to add any pin we want on the map, Just gotta get with Jaiver probably
        to understand if we can come up with default things for the pins for the PinEntity function
        to be simple
    */
/*
    public static PinEntity[] populateData() {
        return new PinEntity[] {
                new PinEntity(255,15, 30.445906,-84.299362,548,"","","Love Building"),
                new PinEntity(255,15, 30.438975,-84.303544,548,"","","Doak Cambell Stadium"),
                new PinEntity(255,15, 30.443845,-84.298055,548,"","","Intergration Statue"),
                new PinEntity(255,15, 30.445031,-84.300000,548,"","","Dirac Science Library"),
                new PinEntity(255,15, 30.445862,-84.301293,548,"","","Keen Building"),
                new PinEntity(255,15, 30.444578,-84.298415,548,"","","Bookstore"),
                new PinEntity(255,15, 30.445494,-84.299762,548,"","","Carothers"),
        };
    }

*/
}
