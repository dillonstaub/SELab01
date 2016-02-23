package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Will on 1/22/2016.
 */

public class AddToContactFragment extends Fragment {

    private static final String TAG = "AddToContact";

    public String placeName;
    public String address;
    public String phoneNumber;

    // These fields are displayed to the user; they may contain prefixed descriptions
    // such as 'Address: ' or 'Phone Number: '
    private TextView placeNameView;
    private TextView addressView;
    private TextView phoneNumberView;

    // These fields are the raw data; they do not contain prefixed descriptions
    // such as 'Address: ' or 'Phone Number: '
    private TextView placeNameViewRaw;
    private TextView addressViewRaw;
    private TextView phoneNumberViewRaw;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addtocontact, container, false);

        // Find the diplayed UI elements
        placeNameView = (TextView) view.findViewById(R.id.addToContact_placeName);
        addressView = (TextView) view.findViewById(R.id.addToContact_address);
        phoneNumberView = (TextView) view.findViewById(R.id.addToContact_phoneNumber);

        // Find the raw UI elements
        placeNameViewRaw = (TextView) view.findViewById(R.id.addToContact_placeNameRaw);
        addressViewRaw = (TextView) view.findViewById(R.id.addToContact_addressRaw);
        phoneNumberViewRaw = (TextView) view.findViewById(R.id.addToContact_phoneNumberRaw);

        UpdateUi();

        return view;
    }

    public void UpdateUi() {
        // Set the displayed UI elements
        placeNameView.setText(placeName);
        addressView.setText("Address: " + address);
        phoneNumberView.setText("Phone Number: " + phoneNumber);

        // Set the raw UI elements
        placeNameViewRaw.setText(placeName);
        addressViewRaw.setText(address);
        phoneNumberViewRaw.setText(phoneNumber);
    }
}
