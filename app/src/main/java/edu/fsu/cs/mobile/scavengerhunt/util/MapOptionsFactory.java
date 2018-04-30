package edu.fsu.cs.mobile.scavengerhunt.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinEntity;

/*
    Helper class for the pins
 */
public class MapOptionsFactory {

    // https://stackoverflow.com/questions/14811579/how-to-create-a-custom-shaped-bitmap-marker-with-android-map-api-v2
    public static Bitmap getMarkerBitmapFromView(Context mContext, Bitmap bitmap, int color) {
        View customMarkerView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_pin, null);
        CircleImageView markerImageView = customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageBitmap(bitmap);
        markerImageView.setBorderColor(color);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    // Source : https://stackoverflow.com/questions/14851641/change-marker-size-in-google-maps-api-v2
    public static Bitmap bitmapSizeByScall(Bitmap bitmapIn, float scall_zero_to_one_f) {

        Bitmap bitmapOut = Bitmap.createScaledBitmap(bitmapIn,
                Math.round(bitmapIn.getWidth() * scall_zero_to_one_f),
                Math.round(bitmapIn.getHeight() * scall_zero_to_one_f), false);

        return bitmapOut;
    }

    public static MarkerOptions generateMarkerOptions(Bitmap pin, String desc, LatLng location) {
        return new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(pin))
                .title(desc)
                .position(location)
                ;
    }


    public static MarkerOptions convertToMO(Context mContext, PinEntity pinEntity) {
        // TODO find a better way to determine size - it'll get messy
        Bitmap decodedBitMap= decodeBLOB(pinEntity.getPath());
        Bitmap pin = MapOptionsFactory.getMarkerBitmapFromView(mContext.getApplicationContext(),decodedBitMap, pinEntity.getColor());
        return new MarkerOptions()
                .title(pinEntity.getDescription())
                .position(new LatLng(pinEntity.getLatitude(), pinEntity.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(pin))
                ;
    }

    public static Bitmap decodeBLOB(byte encodedByteArray[]) {
        return BitmapFactory.decodeByteArray(encodedByteArray, 0, encodedByteArray.length);
    }

    // https://stackoverflow.com/a/10600736/3843432
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
