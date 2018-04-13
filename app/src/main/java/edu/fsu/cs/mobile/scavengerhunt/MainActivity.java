package edu.fsu.cs.mobile.scavengerhunt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.fsu.cs.mobile.scavengerhunt.fragments.DatabaseTestFragment;
import edu.fsu.cs.mobile.scavengerhunt.fragments.FindPinFragment;
import edu.fsu.cs.mobile.scavengerhunt.fragments.LeaderboardFragment;
import edu.fsu.cs.mobile.scavengerhunt.fragments.LoginFragment;
import edu.fsu.cs.mobile.scavengerhunt.fragments.PlacePinFragment;
import edu.fsu.cs.mobile.scavengerhunt.util.PrefManager;

/*
    Main Screen that the user interacts with

 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button bPlace, bFind;
    private static final String TAG = MainActivity.class.getCanonicalName();
    TextView tvTitle;

    // https://medium.com/quick-code/android-navigation-drawer-e80f7fc2594f
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);
        bPlace = findViewById(R.id.place_b);
        bFind = findViewById(R.id.find_b);
        tvTitle = findViewById(R.id.main_title);


        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            inflateFirebaseLogin();

        tvTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                String device_id = FirebaseInstanceId.getInstance().getId();
                Log.d(TAG, "Refreshed token: " + refreshedToken);
                sendRegistrationToServer(refreshedToken, device_id, mAuth.getCurrentUser().getUid());
                return false;
            }
        });

        dl = (DrawerLayout) findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nv = (NavigationView) findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.find:
                        inflateFindFragment();
                    case R.id.place:
                        inflatePlaceFragment();
                    case R.id.messaging:
                        Toast.makeText(MainActivity.this, "Messaging", Toast.LENGTH_SHORT).show();
                    case R.id.friends_list:
                        Toast.makeText(MainActivity.this, "Friends list", Toast.LENGTH_SHORT).show();
                    case R.id.leaderboard:
                        inflateLeaderboard();
                    default:
                        return true;
                }


            }
        });

        bPlace.setOnClickListener(this);
        bFind.setOnClickListener(this);
        //tvTitle.setOnClickListener(this);
    }


    private void sendRegistrationToServer(final String refreshedToken, final String device_id, final String uid) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("FCM_tokens").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> map = task.getResult().getData();
                            // Used for making sure that we are working with the same device
                            // Meaning that client_tokens[0] is from device_ids[0]
                            boolean shouldUpdate = true;
                            ArrayList<String> client_tokens_list = new ArrayList<>();
                            ArrayList<String> device_ids_list = new ArrayList<>();
                            if (map != null) {
                                // Search to see if our client ID is already logged
                                if (map.containsKey("client_tokens")) {
                                    // Get the information from the document
                                    client_tokens_list = (ArrayList<String>) map.get("client_tokens");
                                    if (client_tokens_list.contains(refreshedToken)) {
                                        shouldUpdate = false;
                                    } else {
                                        shouldUpdate = true;
                                        // Get the device_id list so we can append to it
                                        device_ids_list = (ArrayList<String>) map.get("device_ids");
                                        client_tokens_list.remove("");
                                        device_ids_list.remove("");
                                        client_tokens_list.add(refreshedToken);
                                        device_ids_list.add(device_id);
                                    }
                                }

                                // If our client is not already in the database
                                if (shouldUpdate) {
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("client_tokens", client_tokens_list);
                                    user.put("device_ids", device_ids_list);

                                    // Add a new document with a generated ID
                                    db.collection("FCM_tokens").document(uid).set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "Successfully added new token=" + refreshedToken + " for device id=" + device_id);
                                                }
                                            });

                                }

                            }
                            Log.d(TAG, task.getResult().getId());

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());

                        }
                    }
                });

    }


    /**
     * Inflates our login fragment
     */
    private void inflateFirebaseLogin() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        LoginFragment fragment = new LoginFragment();
        trans.add(R.id.frame, fragment, LoginFragment.FRAGMENT_TAG);
        trans.commit();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.place_b:
                inflatePlaceFragment();
                break;
            case R.id.find_b:
                inflateFindFragment();
                break;
            case R.id.main_title:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                //mAuth.signOut();

                Snackbar snack = Snackbar.make(this.findViewById(android.R.id.content), "You have signed out", Snackbar.LENGTH_LONG);
                snack.show();
                //enterSuperSecretDevDebugMode();
                break;
        }
    }

    private void inflateFindFragment(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        FindPinFragment fragment = new FindPinFragment();
        trans.replace(R.id.frame, fragment, FindPinFragment.FRAGMENT_TAG);
        trans.addToBackStack("Find");
        trans.commit();
    }

    private void inflatePlaceFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        PlacePinFragment fragment = new PlacePinFragment();
        trans.replace(R.id.frame, fragment, PlacePinFragment.FRAGMENT_TAG);
        trans.addToBackStack("Place");
        trans.commit();
    }

    private void inflateLeaderboard(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        LeaderboardFragment fragment = new LeaderboardFragment();
        trans.replace(R.id.frame, fragment, LeaderboardFragment.FRAGMENT_TAG);
        trans.addToBackStack("Leader");
        trans.commit();
    }

    // Inflates the fragment for testing database stuff
    private void enterSuperSecretDevDebugMode() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        DatabaseTestFragment fragment = new DatabaseTestFragment();
        trans.add(R.id.frame, fragment, DatabaseTestFragment.FRAGMENT_TAG);
        trans.commit();
    }
}
