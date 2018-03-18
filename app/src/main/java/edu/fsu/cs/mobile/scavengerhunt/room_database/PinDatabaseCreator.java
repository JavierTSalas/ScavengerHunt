package edu.fsu.cs.mobile.scavengerhunt.room_database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/*
 Okay I'm going to be completely honest here, I have NO IDEA how this works
 but apparently it's thread safe and that's a good thing? This is how I had it
 in my app but I can't find the source anywhere...
 */
public class PinDatabaseCreator {
    private static final String TAG = PinDatabaseCreator.class.getCanonicalName();
    private static final Object LOCK = new Object();
    private static PinDatabaseCreator sInstance;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();
    private final AtomicBoolean mInitializing = new AtomicBoolean(true);
    private PinDatabase mDb;

    private PinDatabaseCreator() {
        mIsDatabaseCreated.setValue(false);
    }

    // Lock for ensuring this method is thread-safe
    public synchronized static PinDatabaseCreator getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new PinDatabaseCreator();
                }
            }
        }
        return sInstance;
    }

    /**
     * Used to observe when the database initialization is done
     */
    public LiveData<Boolean> isDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    @Nullable
    public PinDatabase getDatabase() {
        return mDb;
    }

    /**
     * Creates or returns a previously-created database.
     * <p>
     * Although this uses an AsyncTask which currently uses a serial executor, it's thread-safe.
     */
    public void createDb(Context context) {

        Log.d(TAG, "Creating DB from " + Thread.currentThread().getName());

        if (!mInitializing.compareAndSet(true, false)) {
            return; // Already initializing
        }

        // If there is no database then make one
        if (mIsDatabaseCreated.getValue() == false)
            new AsyncTask<Context, Void, Void>() {

                @Override
                protected Void doInBackground(Context... params) {
                    Log.d(TAG, "Starting bg job " + Thread.currentThread().getName());

                    Context context = params[0].getApplicationContext();

                    // Reset the database to have new data on every run.
                    // context.deleteDatabase(PinDatabase.DATABASE_NAME);

                    // Build the database!
                    PinDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                            PinDatabase.class, PinDatabase.DATABASE_NAME).build();

                    // Add some data to the database
                    // SchoolDatabaseInitUtil.initializeDb(db, context);
                    Log.d(TAG, "DB was populated in thread " + Thread.currentThread().getName());

                    mDb = db;
                    return null;
                }

                @Override
                protected void onPostExecute(Void ignored) {
                    // Now on the main thread, notify observers that the db is created and ready.
                    mIsDatabaseCreated.setValue(true);
                }
            }.execute(context.getApplicationContext());
    }

}
