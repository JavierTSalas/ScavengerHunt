package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

import edu.fsu.cs.mobile.scavengerhunt.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LeaderboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LeaderboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeaderboardFragment extends Fragment {
    public static final String FRAGMENT_TAG = "Leader_Fragment";
    ArrayList<TextView> namesViews;
    ArrayList<TextView> scoreViews;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        namesViews = new ArrayList<>();
        scoreViews = new ArrayList<>();

        //I understand this is a terrible way to do this, I will fix this in part 3

        TextView temp;
        
        temp = v.findViewById(R.id.name1);
        namesViews.add(temp);
        temp = v.findViewById(R.id.name2);
        namesViews.add(temp);
        temp = v.findViewById(R.id.name3);
        namesViews.add(temp);
        temp = v.findViewById(R.id.name4);
        namesViews.add(temp);
        temp = v.findViewById(R.id.name5);
        namesViews.add(temp);

        temp = v.findViewById(R.id.score1);
        scoreViews.add(temp);
        temp = v.findViewById(R.id.score2);
        scoreViews.add(temp);
        temp = v.findViewById(R.id.score3);
        scoreViews.add(temp);
        temp = v.findViewById(R.id.score4);
        scoreViews.add(temp);
        temp = v.findViewById(R.id.score5);
        scoreViews.add(temp);

        displayScores();

        return v;
    }

    private void displayScores(){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("high_scores").document("master").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = task.getResult().getData();

                    for (int i = 0; i < 5; i++) {
                        ArrayList<Object> l = (ArrayList<Object>) map.get("" + i);
                        namesViews.get(i).setText( (String)l.get(0) );
                        scoreViews.get(i).setText( "" + l.get(1) );
                    }
                }
            }
        });
    }
}