package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabase;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabaseCreator;

import static android.app.Activity.RESULT_OK;

public class PlacePinFragment extends Fragment {
    private static final String TAG = PlacePinFragment.class.getCanonicalName();
    public static final String FRAGMENT_TAG = "Place_Fragment";

    MapView mMapView;
    private GoogleMap googleMap;
    private boolean mLocationPermissionGranted = false;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1; // I don't think the number matters?
    private double lat, lng;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.place_menu:
                // Get our information about the pin
                openPlaceDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called from our GetPinInfoFragment
     * @param requestCode   RESULT_OK
     * @param resultCode    GetPinInfoFragment.DIALOG_FRAGMENT_REQUEST
     * @param data          Intent with a bundle in it containing the pinID that was just inserted into database
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GetPinInfoFragment.DIALOG_FRAGMENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    Long id = data.getLongExtra(GetPinInfoFragment.UNIQUE_KEY, 0);
                    Log.d(TAG, "Received " + String.valueOf(id) + " from intent");

                    (new placePinGivenId(getActivity().getApplicationContext(), id)).execute();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openPlaceDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        GetPinInfoFragment dialogFragment = GetPinInfoFragment.newInstance(new LatLng(lat, lng));
        dialogFragment.setTargetFragment(this, GetPinInfoFragment.DIALOG_FRAGMENT_REQUEST);
        ((DialogFragment) dialogFragment).show(fm, GetPinInfoFragment.FRAGMENT_TAG);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.place, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

            // For zooming automatically to the location of the marker
            CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            // Set our location listener now that we have permission
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
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

        } else {
            getLocationPermission();
        }
    }


    private class placePinGivenId extends AsyncTask<Void, Void, Void> {
        Context mContext;
        Long pinId;
        MarkerOptions mapOptions;
        final PinDatabaseCreator creator;

        placePinGivenId(Context mContext, long id) {
            this.mContext = mContext;
            this.pinId = id;
            creator = PinDatabaseCreator.getInstance(mContext);
        }

        @Override
        protected void onPreExecute() {
            if (creator.isDatabaseCreated().getValue().equals(Boolean.FALSE))
                creator.createDb(mContext);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PinDatabase db = creator.getDatabase();
            mapOptions = db.PinsDao().getSingleEntity(pinId).convertToMO();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            googleMap.addMarker(mapOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapOptions.getPosition(), 15));
            super.onPostExecute(aVoid);
        }
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
            getLocationPermission(); //Persistence is key
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
}
