package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import edu.fsu.cs.mobile.scavengerhunt.R;


public class SettingsFragment extends Fragment {


    private CheckBox friendCheck;
    private CheckBox onlineCheck;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        friendCheck = v.findViewById(R.id.friend_check);
        onlineCheck = v.findViewById(R.id.online_check);

        return v;
    }

    private boolean isFriendChecked(){
        return friendCheck.isChecked();
    }

    private boolean isOnlineChecked(){
        return onlineCheck.isChecked();
    }
}