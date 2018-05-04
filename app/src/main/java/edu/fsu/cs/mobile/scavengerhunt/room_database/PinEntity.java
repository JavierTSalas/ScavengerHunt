package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.util.MapOptionsFactory;


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


    public PinEntity() {
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


    public void setPinID(@NonNull String pinID) {
        this.pinID = pinID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setPath(byte[] path) {
        this.path = path;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimePlaced(Date timePlaced) {
        this.timePlaced = timePlaced;
    }

    // For sending to firebase
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("pinID", this.pinID);
        objectHashMap.put("userID", this.userID);
        objectHashMap.put("latitude", this.latitude);
        objectHashMap.put("longitude", this.longitude);
        objectHashMap.put("color", this.color);
        objectHashMap.put("pickedUp", this.pickedUp);
        objectHashMap.put("description", this.description);
        objectHashMap.put("timePlaced", this.timePlaced);
        return objectHashMap;
    }

    // For getting from firebase
    public PinEntity(Map<String, Object> objectHashMap, Context mContext) {
        this.pinID = (String) objectHashMap.get("pinID");
        this.userID = (String) objectHashMap.get("userID");
        this.longitude = (double) objectHashMap.get("longitude");
        this.latitude = (double) objectHashMap.get("latitude");
        this.color = (int) (long) (objectHashMap.get("color"));

        // Initialize to default image - Don't want to do this right now 
        Drawable drawable = AppCompatResources.getDrawable(mContext, R.drawable.camera);
        Bitmap selectedBitmap;

        selectedBitmap = MapOptionsFactory.drawableToBitmap(drawable);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);

        this.path = outputStream.toByteArray();
        this.pickedUp = (boolean) objectHashMap.get("pickedUp");
        this.description = (String) objectHashMap.get("description");
        this.timePlaced = (Date) objectHashMap.get("timePlaced");
    } 
}
