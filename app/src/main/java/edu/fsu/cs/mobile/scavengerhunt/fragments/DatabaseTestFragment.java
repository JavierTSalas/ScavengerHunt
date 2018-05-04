package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

import edu.fsu.cs.mobile.scavengerhunt.R;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabase;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinDatabaseCreator;
import edu.fsu.cs.mobile.scavengerhunt.room_database.PinEntity;

public class DatabaseTestFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = DatabaseTestFragment.class.getCanonicalName();
    public static final String FRAGMENT_TAG = "DATABASE_FRAGMENT";
    TextView tvMain;
    EditText etPinId, etUserPid, etLongitude, etLatitude, etColor, etPath, etDescription;
    Button bInsert, bFetch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_database, container, false);

        tvMain = view.findViewById(R.id.database_fragment_title_text);

        etPinId = view.findViewById(R.id.database_fragment_pinID_edit);
        etUserPid = view.findViewById(R.id.database_fragment_userID_edit);
        etLongitude = view.findViewById(R.id.database_fragment_longitude_edit);
        etLatitude = view.findViewById(R.id.database_fragment_latitude_edit);
        etColor = view.findViewById(R.id.database_fragment_color_edit);
        etPath = view.findViewById(R.id.database_fragment_path_edit);
        etDescription = view.findViewById(R.id.database_fragment_desc_edit);
        bFetch = view.findViewById(R.id.fetch_query_button);
        bInsert = view.findViewById(R.id.insert_query_button);

        bFetch.setOnClickListener(this);
        bInsert.setOnClickListener(this);

        return view;
    }

    /**
     * Populates our EditTexts with data from param
     *
     * @param pinEntity
     */
    private void fillOutFields(PinEntity pinEntity) {
        etPinId.setText(String.valueOf(pinEntity.getPinID()));
        etUserPid.setText(String.valueOf(pinEntity.getUserID()));
        etLongitude.setText(String.valueOf(pinEntity.getLongitude()));
        etLatitude.setText(String.valueOf(pinEntity.getLatitude()));
        etColor.setText(String.valueOf(pinEntity.getColor()));
        etPath.setText(pinEntity.getPath().toString());
        etDescription.setText(pinEntity.getDescription());
    }

    /**
     * Gets our information from our EditTexts to inset into a PinEntity object
     *
     * @return User Defined PinEntity
     */
    private PinEntity extractFromFields() {
        //    Constructor => PinEntity(long pinID, long userID, long longitude, long latitude, Color color, URI path, String description)
        return new PinEntity(
                "Sample",
                "This is deprecated",
                Long.parseLong(etLongitude.getText().toString()),
                Long.parseLong(etLatitude.getText().toString()),
                // COLORS HAVE TO START WITH #
                Color.parseColor(etColor.getText().toString()),
                new byte[]{0, 0, 0}, // This is weird but refer to the documentation provided in PinEntity
                false,
                etDescription.getText().toString(),
                new Date());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.insert_query_button:
                // We need to do all queries using tasks...
                // Well we don't NEED to but it's better to
                (new InsertTask(extractFromFields(), getContext())).execute();
                break;
            case R.id.fetch_query_button:
                (new FetchTask(etPinId.getText().toString(), getContext())).execute();
                break;
        }
    }

    private class InsertTask extends AsyncTask<Void, Void, Void> {
        int countOfEntities;
        PinEntity EntityToInsert;
        Context mContext;
        final PinDatabaseCreator creator;

        public InsertTask(PinEntity entityToInsert, Context mContext) {
            EntityToInsert = entityToInsert;
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
            database.PinsDao().insert(EntityToInsert);
            countOfEntities = database.PinsDao().getCountOfEntities();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String text = "There are " + countOfEntities + " elements in the database!";
            tvMain.setText(text);
            super.onPostExecute(aVoid);
        }
    }


    private class FetchTask extends AsyncTask<Void, Void, Void> {
        String IndexToFetch;
        Context mContext;
        final PinDatabaseCreator creator;
        PinEntity entity;

        public FetchTask(String string, Context mContext) {
            IndexToFetch = string;
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
            entity = database.PinsDao().getSingleEntity(IndexToFetch);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            fillOutFields(entity);
            super.onPostExecute(aVoid);
        }
    }
}
