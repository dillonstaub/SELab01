package com.mapbox.mapboxsdk.android.testapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapbox.mapboxsdk.android.testapp.ui.NavigationInfoWindow;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import com.mapbox.mapboxsdk.android.testapp.NavigationFragment;

import java.util.List;

/**
 * Created by Will on 1/22/2016.
 */

public class NavigationRoutesFragment extends Fragment {
    private static final String TAG = "NavigationRoutes";

    public NavigationFragment navFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        // Find all the UI elements
        Button navigationAddressButton = (Button) view.findViewById(R.id.navigation_address_button);
        final LinearLayout addressBar = (LinearLayout) view.findViewById(R.id.navigationAddressBar);
        final EditText addressTextBox = (EditText) view.findViewById(R.id.navigation_address);
        final MapView mapView = (MapView) view.findViewById(R.id.navigationMapView);
/*
        // Set the listener for clicking "search"
        navigationAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationSearchButton(v, addressBar, addressTextBox, mapView);
            }
        });*/



        return view;
    }

}
