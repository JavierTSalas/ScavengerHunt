package edu.fsu.cs.mobile.scavengerhunt;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.fsu.cs.mobile.scavengerhunt.fragments.databaseTestFragment;

/*
    Main Screen that the user interacts with
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button bPlace, bFind;
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bPlace = findViewById(R.id.place_b); // place_b? What is this blasphemy!
        bFind = findViewById(R.id.find_b); // And thus the naming convention war begun
        tvTitle = findViewById(R.id.main_title);


        tvTitle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.place_b:
                break;
            case R.id.find_b:
                break;
            case R.id.main_title:
                // Who really cares about functions names when you have auto complete?
                enterSuperSecretDevDebugMode();
                break;
        }
    }

    // Inflates the fragment for testing database stuff
    private void enterSuperSecretDevDebugMode() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        databaseTestFragment fragment = new databaseTestFragment();
        trans.add(R.id.frame, fragment, fragment.FRAGMENT_TAG);
        trans.commit();
    }
}
