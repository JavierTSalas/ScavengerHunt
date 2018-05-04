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
import android.graphics.drawable.Drawable;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabase;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabaseCreator;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinEntity;
import edu.fsu.cs.mobile.scavengerhunt.util.MapOptionsFactory;
import edu.fsu.cs.mobile.scavengerhunt.util.PlaceJSON;
import edu.fsu.cs.mobile.scavengerhunt.util.md5hasher;

public class FindPinFragment extends Fragment {
    private static final String TAG = FindPinFragment.class.getCanonicalName();
    public static final String FRAGMENT_TAG = "Find_Fragment";

    private String type = "park";

    private long points;

    MapView mMapView;
    TextView mTemperature;
    FloatingActionButton mNearPin;
    private GoogleMap googleMap;
    private HashMap<String, PinWrapper> pinObjs = new HashMap<>();
    private HashMap<String, Integer> pHistory = new HashMap<>();
    private Location lastLoc = null;


    private boolean mLocationPermissionGranted = false;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1; // I don't think the number matters?
    private double lat = Long.MAX_VALUE;
    private double lng = Long.MAX_VALUE;
    private int pinSessionCounter = 0;
    final private float HOT_DISTANCE = 60;

    final private float FIND_DISTANCE = 50;
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

        mTemperature = view.findViewById(R.id.temp_id);

        mNearPin = view.findViewById(R.id.near_button);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        points = 0;

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
                    if(isAdded()){
                        if(lastLoc == null || lastLoc.distanceTo(location) > 500){
                            lastLoc = location;
                            new getAllPins(getActivity().getApplicationContext(), new LatLng(location.getLatitude(), location.getLongitude())).execute();
                        }

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
            Location tempLoc = new Location("Initial Loc");
            tempLoc.setLatitude(currentLocation.latitude);
            tempLoc.setLongitude(currentLocation.longitude);

            locationListener.onLocationChanged(tempLoc);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);


        } else {
            getLocationPermission();
        }
    }

    //Counting each pin found during one session.
    private void PinFound(String name){
        pinSessionCounter++;

        points += 100;

        if(isAdded()){
            Toast.makeText(getActivity().getApplicationContext(), "Pins found this session: " + pinSessionCounter , Toast.LENGTH_LONG).show();
        }


    }


    //Finds the closest pin, but makes pins that are too close visible
    private float findClosestPin(Location mLoc){
        if(pinObjs.size() > 0) {
            //We are trying to find the smallest val, so we'll initialize it with the first one
            float small = -1;
            RoundedBitmapDrawable rounded = null;

            for(String k : pinObjs.keySet()){
                Location temp = new Location("Pin " + k);
                temp.setLatitude(pinObjs.get(k).getMyPin().getLatitude());
                temp.setLongitude(pinObjs.get(k).getMyPin().getLongitude());

                if(!pinObjs.get(k).isPickedUp()){
                    if(mLoc.distanceTo(temp) <= small || mLoc.distanceTo(temp) <= FIND_DISTANCE || small < 0){
                        if(mLoc.distanceTo(temp) <= FIND_DISTANCE){

                            //Adds a marker for this to the map
                            googleMap.addMarker(pinObjs.get(k).getMyMarker());
                            pinObjs.get(k).setPickedUp(true);

                            PinFound(pinObjs.get(k).getMyPin().getDescription());
                        }
                        else{
                            small = mLoc.distanceTo(temp);

                            //Sets up a rounded bitmap for the nearest pin
                            //Found on https://stackoverflow.com/questions/24878740/how-to-use-roundedbitmapdrawable
                            Bitmap square = MapOptionsFactory.decodeBLOB(pinObjs.get(k).getMyPin().getPath());
                            rounded = RoundedBitmapDrawableFactory.create(getResources(), square);
                            rounded.setCornerRadius(Math.min(square.getWidth(), square.getHeight()) / 2.0f);

                        }
                    }
                }
            }

            //Sets the nearPin button to hold that rounded bitmap
            mNearPin.setImageDrawable(rounded);

            Log.i("Booo", "Returned a distance of " + small);

            //Returns the distance
            return small;
        }
        return -1;
    }

    //Followed the following
    //https://stackoverflow.com/questions/41799824/google-map-to-find-current-location-and-nearby-places
    //https://stackoverflow.com/questions/30161395/im-trying-to-search-nearby-places-such-as-banks-restaurants-atms-inside-the-d
    public StringBuilder sb(){
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(lat).append(",").append(lng);
        googlePlacesUrl.append("&radius=").append(750);
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&key=").append(getString(R.string.google_places_key));

        return googlePlacesUrl;
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
        updateDatabase();
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

    private void updateDatabase(){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        db.collection("high_scores").document("master").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = task.getResult().getData();
                    Map<String, Object> newScores = new HashMap<>();

                    for (int i = 0; i < 5; i++) {
                        ArrayList<Object> l = (ArrayList<Object>) map.get("" + i);

                        if((long) l.get(1) < points){

                            ArrayList<Object> temp = new ArrayList<>();
                            ArrayList<Object> temp2 = new ArrayList<>();

                            temp.add(mAuth.getCurrentUser().getDisplayName());
                            temp.add(points);
                            newScores.put(""+i, temp);

                            temp2.add(l.get(0));
                            temp2.add(l.get(1));

                            for(int j = i + 1; j < 5; j++){
                                ArrayList<Object> l2 = (ArrayList<Object>) map.get("" + j);
                                newScores.put(""+j, temp2);
                                temp2 = new ArrayList<>();
                                temp2.add(l2.get(0));
                                temp2.add(l2.get(1));
                            }
                            break;
                        } else {
                            ArrayList<Object> temp = new ArrayList<>();
                            temp.add(l.get(0));
                            temp.add(l.get(1));
                            newScores.put(""+i,temp);
                        }

                    }

                    db.collection("high_scores").document("master").set(newScores).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.i(TAG, "Successfully added my score!");
                        }
                    });
                }
            }
        });
    }

    private void updateLocationBackend(){

    }

    private class getAllPins extends AsyncTask<Void, Void, Void> {
        Context mContext;
        ArrayList<MarkerOptions> allMO = new ArrayList<MarkerOptions>();
        ArrayList<PinEntity> allPins = new ArrayList<PinEntity>();
        final PinDatabaseCreator creator;
        LatLng curLoc;

        getAllPins(Context mContext, LatLng curLoc) {
            this.mContext = mContext;
            creator = PinDatabaseCreator.getInstance(mContext);
            this.curLoc = curLoc;
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

            Log.i("Boooo", "DB Query returned a size of " + allPins.size());

            for(int i = 0; i < allPins.size(); i++){
                allMO.add(MapOptionsFactory.convertToMO(mContext, allPins.get(i)));

            }

            StringBuilder sbValue = new StringBuilder(sb());
            PlacesTask placesTask = new PlacesTask(mContext);
            placesTask.execute(sbValue.toString());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


            for(int i = 0; i < allMO.size(); i++){

                if(pinObjs.get(allPins.get(i).getDescription()) == null){
                    PinWrapper tempPw = new PinWrapper(allPins.get(i), allMO.get(i), true);
                    pinObjs.put(tempPw.getMyPin().getDescription(), tempPw);
                }

            }


            super.onPostExecute(aVoid);
        }
    }

    private class PlacesTask extends AsyncTask<String, Integer, String>{
        String val = null;
        Context mContext;

        PlacesTask(Context mContext){
            this.mContext = mContext;
        }

        @Override
        protected String doInBackground(String... url) {
            try {
                val = getUrl(url[0]);
            } catch (Exception e) {
                Log.i("Uh Oh", e.toString());
            }
            return val;
        }

        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask(mContext);

            parserTask.execute(result);
        }

        protected String getUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                //Creates httpurlconnection based on the inputted url
                urlConnection = (HttpURLConnection) url.openConnection();

                //Connects to this url
                urlConnection.connect();

                // pulls the data from this url
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuffer sb = new StringBuffer();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                Log.d("Uh Oh", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;
        Context mContext;

        ParserTask(Context context){
            mContext = context;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSON placeJson = new PlaceJSON();
            Log.i(TAG, jsonData[0]);

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            for (int i = 0; i < list.size(); i++) {
                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                //Pull location
                double tempLat = Double.parseDouble(hmPlace.get("lat"));
                double tempLng = Double.parseDouble(hmPlace.get("lng"));

                //Pull pin's name
                String name = hmPlace.get("place_name");

                int tempColor = Color.BLACK;

                Log.d("Map", "place: " + name);

                LatLng latLng = new LatLng(tempLat, tempLng);

                String imageUrl = hmPlace.get("imageUrl");
                Log.i(TAG, "Image URL is " + imageUrl);

                Drawable d = getResources().getDrawable(R.drawable.default_pin);

                Bitmap iconBitmap= ((BitmapDrawable) d).getBitmap();

                byte[] BLOB;

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                iconBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                BLOB = outputStream.toByteArray();

                Date now = new Date();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String unsalted_key = now.hashCode() + "_" + userId;
                String saltedKey = md5hasher.md5(unsalted_key);

                PinEntity pe = new PinEntity(
                        saltedKey,
                        userId, // For later use, now just 0
                        tempLat,
                        tempLng,
                        tempColor,
                        BLOB,
                        false,
                        name,
                        now
                );

                MarkerOptions mo = MapOptionsFactory.convertToMO(mContext, pe);

                if(pinObjs.get(pe.getDescription()) == null){
                    PinWrapper tempPw = new PinWrapper(pe, mo, false);
                    pinObjs.put(tempPw.getMyPin().getDescription(), tempPw);
                }

            }
        }
    }


    private class PinWrapper{
        private PinEntity myPin;
        private MarkerOptions myMarker;
        private boolean pickedUp;
        private boolean placesPin;
        private boolean userPin;

        public PinWrapper(PinEntity myPin, MarkerOptions myMarker, boolean user){
            this.myPin = myPin;
            this.myMarker = myMarker;
            pickedUp = false;
            placesPin = !user;
            userPin = user;
        }

        public PinEntity getMyPin() {
            return myPin;
        }

        public MarkerOptions getMyMarker() {
            return myMarker;
        }

        public boolean isPickedUp() {
            return pickedUp;
        }

        public void setPickedUp(boolean pickedUp) {
            this.pickedUp = pickedUp;
        }

        public boolean isPlacesPin() {
            return placesPin;
        }

        public void setPlacesPin(boolean placesPin) {
            this.placesPin = placesPin;
        }

        public boolean isUserPin() {
            return userPin;
        }

        public void setUserPin(boolean userPin) {
            this.userPin = userPin;
        }

    }
}
