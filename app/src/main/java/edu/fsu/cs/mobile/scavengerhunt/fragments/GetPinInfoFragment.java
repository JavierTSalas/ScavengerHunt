package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabase;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabaseCreator;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinEntity;
import edu.fsu.cs.mobile.scavengerhunt.util.MapOptionsFactory;

import static android.app.Activity.RESULT_OK;

public class GetPinInfoFragment extends DialogFragment implements View.OnClickListener {
    public static final String FRAGMENT_TAG = "GetPinInfo_Fragment";
    private static final String TAG = GetPinInfoFragment.class.getCanonicalName();
    private static final String LatKey = "Latitude";
    private static final String LngKey = "Longitude";
    public static final String UNIQUE_KEY = "UniqueId";
    public static final int DIALOG_FRAGMENT_REQUEST = 2; // I don't think the number matters?

    double Lat, Lng;
    private int selectedColorRGB = Color.BLACK;
    private MapView MapPreview;
    private View vColorPreview;
    private EditText etDesc;
    private Button bSubmit, bPicture, bClear, bColor;
    private Bitmap selectedBitmap;
    private GoogleMap googleMap;


    /**
     * Constructor for creating an instance with a LatLng
     *
     * @param latLng
     * @return GetPinInfoFragment with information as local data
     */
    public static GetPinInfoFragment newInstance(LatLng latLng) {
        Bundle args = new Bundle();
        GetPinInfoFragment fragment = new GetPinInfoFragment();
        args.putDouble(LatKey, latLng.latitude);
        args.putDouble(LngKey, latLng.longitude);
        fragment.setArguments(args);
        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            Lat = bundle.getDouble(LatKey);
            Lng = bundle.getDouble(LngKey);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_dialog, container, false);
        readBundle(getArguments());

        MapPreview = view.findViewById(R.id.dialog_preview_mapview);
        MapPreview.onCreate(savedInstanceState);

        MapPreview.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MapPreview.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                clearImagePreview();
                googleMap.addMarker(generateMarker());
            }
        });



        bPicture = view.findViewById(R.id.dialog_gallery_button);
        bClear = view.findViewById(R.id.dialog_clear_button);
        bColor = view.findViewById(R.id.dialog_color_button);
        bSubmit = view.findViewById(R.id.dialog_submit_button);
        vColorPreview = view.findViewById(R.id.dialog_color_view);
        etDesc = view.findViewById(R.id.input_desc);

        bPicture.setOnClickListener(this);
        bClear.setOnClickListener(this);
        bColor.setOnClickListener(this);
        bSubmit.setOnClickListener(this);




        return view;
    }


    private MarkerOptions generateMarker() {
        googleMap.clear();
        LatLng location = (new LatLng(Lat, Lng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));
        Bitmap pin = MapOptionsFactory.getMarkerBitmapFromView(getActivity().getApplicationContext(), selectedBitmap, selectedColorRGB);
        return MapOptionsFactory.generateMarkerOptions(pin, etDesc.getText().toString(), location);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_gallery_button:
                promptUserForImage();
                break;
            case R.id.dialog_clear_button:
                clearImagePreview();
                googleMap.addMarker(generateMarker());
                break;
            case R.id.dialog_color_button:
                openColorPicker();
                break;
            case R.id.dialog_submit_button:
                if (validFields()) {
                    (new InsertTask(getActivity().getApplicationContext())).execute();
                } else {
                    notifyUserOfInvalidFields();
                    googleMap.addMarker(generateMarker());
                }
                break;
        }
    }

    /**
     * Sets the default image
     */
    private void clearImagePreview() {
        Drawable drawable = AppCompatResources.getDrawable(getActivity().getApplicationContext(), R.drawable.camera);
        selectedBitmap = MapOptionsFactory.drawableToBitmap(drawable);
        googleMap.addMarker(generateMarker());
    }

    /**
     * Opens color picker for result
     */
    private void openColorPicker() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ColorPickFragment colorPickFragment = new ColorPickFragment();
        colorPickFragment.setTargetFragment(this, ColorPickFragment.DIALOG_FRAGMENT_REQUEST);
        ((DialogFragment) colorPickFragment).show(fm, ColorPickFragment.FRAGMENT_TAG);


    }

    /*
        Checks for valid fields
     */
    private boolean validFields() {
        boolean ValidImageUri = selectedBitmap != null;
        boolean ValidDesc = !etDesc.getText().toString().isEmpty();
        return ValidImageUri && ValidDesc;
    }

    // TODO: Find a better way to do this
    private void notifyUserOfInvalidFields() {
        Toast.makeText(getActivity().getApplicationContext(), "SIR, YOU NEED TO FILL OUT ALL THE FIELDS", Toast.LENGTH_SHORT).show();
    }


    /**
     *
     * Communication with PlacePinFragment
     * @param REQUEST_CODE
     * @param id            our pinID
     */
    private void sendResult(int REQUEST_CODE,long id) {
        Intent intent = new Intent();
        intent.putExtra(UNIQUE_KEY, id);
        getTargetFragment().onActivityResult(REQUEST_CODE,RESULT_OK, intent);
    }


    /**
     * Communication with ColorPickFragment
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ColorPickFragment.DIALOG_FRAGMENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    selectedColorRGB = intent.getIntExtra(ColorPickFragment.COLOR_KEY, Color.parseColor("#782F40"));
                    vColorPreview.setBackgroundColor(selectedColorRGB);
                    Log.d(TAG, "Received " + String.valueOf(selectedColorRGB) + " from intent");
                    googleMap.addMarker(generateMarker());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * ...prompts the user for an image...
     */
    private void promptUserForImage() {
        PickImageDialog.build(new PickSetup())
                .setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult r) {
                        if (r.getError() == null) {
                            selectedBitmap = r.getBitmap();
                            googleMap.addMarker(generateMarker());
                        }
                    }
                })
                .setOnPickCancel(new IPickCancel() {
                    @Override
                    public void onCancelClick() {
                        //TODO: do what you have to if user clicked cancel
                    }
                }).show(getActivity().getSupportFragmentManager());

    }

    private class InsertTask extends AsyncTask<Void, Void, Void> {
        Context mContext;
        final PinDatabaseCreator creator;
        private long idOfNewEntry;
        byte[] BLOB;

        public InsertTask(Context mContext) {
            this.mContext = mContext;
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
            // Generate our BLOB
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (selectedBitmap != null) {
                selectedBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                BLOB = outputStream.toByteArray();
            }

            PinEntity EntityToInsert = new PinEntity(
                    0,
                    0, // For later use, now just 0
                    Lat,
                    Lng,
                    selectedColorRGB,
                    BLOB,
                    false,
                    etDesc.getText().toString());
            database.PinsDao().insert(EntityToInsert);
            idOfNewEntry = database.PinsDao().getPinIDOfLastEntry();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String text = "Inserted element " + idOfNewEntry ;
            Log.d(TAG, text);
            sendResult(DIALOG_FRAGMENT_REQUEST,idOfNewEntry);
            dismiss();
            super.onPostExecute(aVoid);
        }
    }

}
