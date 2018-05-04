package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabase;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabaseCreator;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinEntity;
import edu.fsu.cs.mobile.scavengerhunt.util.MapOptionsFactory;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;

public class PlacePinFragment extends Fragment {
    private static final String TAG = PlacePinFragment.class.getCanonicalName();
    public static final String FRAGMENT_TAG = "Place_Fragment";

    MapView mMapView;
    private GoogleMap googleMap;
    private boolean mLocationPermissionGranted = false;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1; // I don't think the number matters?
    private double lat = Long.MAX_VALUE;
    private double lng = Long.MAX_VALUE;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place, container, false);
        mMapView = view.findViewById(R.id.mapView);
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

        readBundle(getArguments());

        // Required for using our tool bar
        setHasOptionsMenu(true);

        return view;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            String firebaseId = bundle.getString("firebaseID");
            Log.d(TAG, "Read from bundle=" + firebaseId);
            placePinFromDirebase(firebaseId);
        }
    }

    private void placePinFromDirebase(String firebaseID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pins").document(firebaseID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        PinEntity pe = new PinEntity(document.getData(), getActivity().getApplicationContext());
                        (new InsertTask(getActivity().getApplicationContext(), pe)).execute();

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    private class InsertTask extends AsyncTask<Void, Void, Void> {
        Context mContext;
        PinEntity pinEntity;
        final PinDatabaseCreator creator;
        private MarkerOptions mapOptions;

        public InsertTask(Context mContext, PinEntity pe) {
            this.mContext = mContext;
            this.pinEntity = pe;
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
            PinDatabase database = creator.getDatabase();
            database.PinsDao().insert(pinEntity);
            mapOptions = MapOptionsFactory.convertToMO(mContext, pinEntity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            googleMap.addMarker(mapOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapOptions.getPosition(), 15));
            super.onPostExecute(aVoid);
        }
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
                    String id = data.getStringExtra(GetPinInfoFragment.UNIQUE_KEY);
                    Log.d(TAG, "Received " + String.valueOf(id) + " from intent");

                    (new placePinGivenId(getActivity().getApplicationContext(), id)).execute();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openPlaceDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        GetPinInfoFragment dialogFragment = GetPinInfoFragment.newInstance(generateLatLong());
        dialogFragment.setTargetFragment(this, GetPinInfoFragment.DIALOG_FRAGMENT_REQUEST);
        dialogFragment.show(fm, GetPinInfoFragment.FRAGMENT_TAG);
    }

    /**
     * Constructor for creating an instance with a LatLng
     *
     * @param id
     * @return PlacePinFragment with information as local data
     */
    public static PlacePinFragment newInstance(String id) {
        Bundle args = new Bundle();
        PlacePinFragment fragment = new PlacePinFragment();
        args.putString("firebaseID", id);
        fragment.setArguments(args);
        return fragment;
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
        String pinId;
        MarkerOptions mapOptions;
        final PinDatabaseCreator creator;
        final FirebaseFirestore db = FirebaseFirestore.getInstance();


        placePinGivenId(Context mContext, String id) {
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
            PinDatabase pinDatabase = creator.getDatabase();
            final PinEntity pin = pinDatabase.PinsDao().getSingleEntity(pinId);
            mapOptions = MapOptionsFactory.convertToMO(mContext, pin);

            // Update one field, creating the document if it does not already exist.

            db.collection("pins").document(pinId).set(pin.toHashMap(), SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Wrote id=" + pinId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document id=" + pinId, e);
                }
            });
            Snackbar.make(getActivity().findViewById(android.R.id.content), "Click to copy the pinID ", Snackbar.LENGTH_INDEFINITE).setAction("Click to copy!", new View.OnClickListener() {
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("pinId", pinId);
                    clipboard.setPrimaryClip(clip);
                }
            }).show();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            googleMap.addMarker(mapOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapOptions.getPosition(), 15));

            //This will probably be the spot where making random pins will probably be the easiest



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
