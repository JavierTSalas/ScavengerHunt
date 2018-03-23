package edu.fsu.cs.mobile.scavengerhunt.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.rarepebble.colorpicker.ColorPickerView;

import edu.fsu.cs.mobile.scavengerhunt.R;

import static android.app.Activity.RESULT_OK;

public class ColorPickFragment extends DialogFragment {
    final public static int DIALOG_FRAGMENT_REQUEST = 3;
    public static final String COLOR_KEY = "Color_Key";
    public static String FRAGMENT_TAG = "Color_Picker_Fragment";
    private ColorPickerView colorPicker;
    private Button bConfirm;

    /**
     * For communication with GetPinInfoFragment
     * @param REQUEST_CODE
     */
    private void sendResult(int REQUEST_CODE) {
        Intent intent = new Intent();
        intent.putExtra(COLOR_KEY, colorPicker.getColor());
        getTargetFragment().onActivityResult(REQUEST_CODE,RESULT_OK, intent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.color_picker, container, false);
        colorPicker = view.findViewById(R.id.colorPicker);
        colorPicker.setColor(Color.parseColor("#782F40"));
        bConfirm = view.findViewById(R.id.color_picker_button);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(DIALOG_FRAGMENT_REQUEST);
                dismiss();
            }
        });
        return view;
    }
}
