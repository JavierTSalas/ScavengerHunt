package edu.fsu.cs.mobile.scavengerhunt.fragments;

/*
 * Created by Jose Fernandes
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabase;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabaseCreator;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinEntity;
import edu.fsu.cs.mobile.scavengerhunt.util.MapOptionsFactory;

public class FindPinFragment extends Fragment {
    private static final String TAG = FindPinFragment.class.getCanonicalName();
    public static final String FRAGMENT_TAG = "Find_Fragment";

    MapView mMapView;
    TextView mTemperature;
    FloatingActionButton mNearPin;
    private GoogleMap googleMap;
    private ArrayList<MarkerOptions> allPinMO = new ArrayList<MarkerOptions>();
    private ArrayList<PinEntity> allPin = new ArrayList<PinEntity>();
    private boolean mLocationPermissionGranted = false;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1; // I don't think the number matters?
    private double lat = Long.MAX_VALUE;
    private double lng = Long.MAX_VALUE;
    private int pinSessionCounter = 0;

    final private float FIND_DISTANCE = 50;

    final private float HOT_DISTANCE = 60;
    final private float WARM_DISTANCE = 90;
    final private float COOL_DISTANCE = 150;

    final private String HOT_MESSAGE = "Hot!";
    final private String WARM_MESSAGE = "Warm";
    final private String COOL_MESSAGE = "Cool";
    final private String FREEZING_MESSAGE = "Freezing...";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, container, false);

        mTemperature = (TextView) view.findViewById(R.id.temp_id);

        mNearPin = (FloatingActionButton) view.findViewById(R.id.near_button);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        //Setting up list view and adapter
        //Templist to figure out why listview isn't showing above map
        String[] menuItems = {"Do somthin!", "Anotherthing!","teehee"};

        final ListView listView = (ListView) view.findViewById(R.id.hintList);

        ArrayAdapter<String> pinlist = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                menuItems
        );
        listView.setAdapter(pinlist);


        mMapView.onResume(); // needed to get the map to display immediately

        //May bring in stuff from database, but haven't got the listview to show
       /* ArrayAdapter<MarkerOptions> pinlist = new ArrayAdapter<MarkerOptions>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                allPinMO

        );*/


        //Button to show listview
        Button hintButton = (Button) view.findViewById(R.id.HintButton);
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity().getApplicationContext(), "Temp", Toast.LENGTH_LONG).show();

                listView.setVisibility(listView.isShown() ? View.GONE : View.VISIBLE);
            }
        });



        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if (mLocationPermissionGranted) {
                    updateLocationUI();
                } else {
                    getLocationPermission();
                }
            }
        });


        // Required for using our tool bar
        setHasOptionsMenu(true);

        return view;
    }


    private LatLng generateLatLong() {
        // If these values are not their defaults
        if (mLocationPermissionGranted) {
            if (lat != Long.MAX_VALUE && lng != Long.MAX_VALUE) {
                return new LatLng(lat, lng);
            } else {
                LocationManager locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    @SuppressLint("MissingPermission") Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocationGPS != null) {
                        return new LatLng(lastKnownLocationGPS.getLatitude(), lastKnownLocationGPS.getLongitude());
                    }
                }
            }
        } else {
            getLocationPermission();
        }
        return new LatLng(0, 0);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Simple update to check if we have permission for location
     */
    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (mLocationPermissionGranted) {
            googleMap.setMyLocationEnabled(true);

            // For dropping a marker at a point on the Map
            LatLng currentLocation = generateLatLong();
            // For zooming automatically to the location of the marker
            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            // Set our location listener now that we have permission
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    float dist = findClosestPin(location);

                    if(dist <= HOT_DISTANCE && dist > 0){
                        mTemperature.setText(HOT_MESSAGE);
                        mTemperature.setTextColor(Color.RED);
                    } else if( dist <= WARM_DISTANCE && dist > 0){
                        mTemperature.setText(WARM_MESSAGE);
                        mTemperature.setTextColor(Color.YELLOW);
                    } else if ( dist <= COOL_DISTANCE && dist > 0){
                        mTemperature.setText(COOL_MESSAGE);
                        mTemperature.setTextColor(Color.BLUE);
                    } else {
                        mTemperature.setText(FREEZING_MESSAGE);
                        mTemperature.setTextColor(Color.CYAN);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

            (new getAllPins(getActivity().getApplicationContext())).execute();

        } else {
            getLocationPermission();
        }
    }

    //Counting each pin found during one session.
    private void PinFound(){

        pinSessionCounter++;

       Toast.makeText(getActivity().getApplicationContext(), "Pins found this session: " + pinSessionCounter + "\nGood Job! c:", Toast.LENGTH_LONG).show();


    }


    //Finds the closest pin, but makes pins that are too close visible
    private float findClosestPin(Location mLoc){
        if(allPinMO.size() > 0) {
            Location t = new Location("First Pin");
            t.setLatitude(allPinMO.get(0).getPosition().latitude);
            t.setLongitude(allPinMO.get(0).getPosition().longitude);

            //We are trying to find the smallest val, so we'll initialize it with the first one
            float small = -1;
            for (int i = 1; i < allPinMO.size(); i++) {
                Location temp = new Location("Pin " + i);
                temp.setLatitude(allPinMO.get(i).getPosition().latitude);
                temp.setLongitude(allPinMO.get(i).getPosition().longitude);

                if(mLoc.distanceTo(temp) <= small || mLoc.distanceTo(temp) <= FIND_DISTANCE || small < 0){
                    if(mLoc.distanceTo(temp) <= FIND_DISTANCE){
                        //Sets the nearest to null in the text and image
                        small = -1;
                        mNearPin.setImageDrawable(null);

                        //Adds a marker for this to the map
                        googleMap.addMarker(allPinMO.get(i));
                        allPinMO.remove(i);
                        
                        PinFound();
                        break; //For some reason looped 5 times, guess it is checking stuff?

                    }
                    else{
                        small = mLoc.distanceTo(temp);

                        //Sets up a rounded bitmap for the nearest pin
                        //Found on https://stackoverflow.com/questions/24878740/how-to-use-roundedbitmapdrawable
                        Bitmap square = MapOptionsFactory.decodeBLOB(allPin.get(i).getPath());
                        RoundedBitmapDrawable rounded = RoundedBitmapDrawableFactory.create(getResources(), square);
                        rounded.setCornerRadius(Math.min(square.getWidth(), square.getHeight()) / 2.0f);

                        //Sets the nearPin button to hold that rounded bitmap
                        mNearPin.setImageDrawable(rounded);
                    }
                }
            }

            return small;
        }
        return -1;
    }



    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            getLocationPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private class getAllPins extends AsyncTask<Void, Void, Void> {
        Context mContext;
        ArrayList<MarkerOptions> allMO = new ArrayList<MarkerOptions>();
        ArrayList<PinEntity> allPins = new ArrayList<PinEntity>();
        final PinDatabaseCreator creator;

        getAllPins(Context mContext) {
            this.mContext = mContext;
            creator = PinDatabaseCreator.getInstance(mContext);
        }

        @Override
        protected void onPreExecute() {
            if (creator.isDatabaseCreated().getValue().equals(Boolean.FALSE)) {
                creator.createDb(mContext);
            }
            googleMap.clear();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PinDatabase db = creator.getDatabase();

            allPins = (ArrayList<PinEntity>) db.PinsDao().getAllPins();
            for(int i = 0; i < allPins.size(); i++){
                allMO.add(MapOptionsFactory.convertToMO(mContext, allPins.get(i)));
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for(int i = 0; i < allMO.size(); i++){
                allPinMO.add(allMO.get(i));
                allPin.add(allPins.get((i)));
            }
            super.onPostExecute(aVoid);
        }
    }
}
