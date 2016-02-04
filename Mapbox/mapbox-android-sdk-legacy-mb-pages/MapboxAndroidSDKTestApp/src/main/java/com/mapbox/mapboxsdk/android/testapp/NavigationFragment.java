package com.mapbox.mapboxsdk.android.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.mapbox.mapboxsdk.android.testapp.ui.CustomInfoWindow;
import com.mapbox.mapboxsdk.android.testapp.ui.NavigationInfoWindow;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mapbox.mapboxsdk.util.DataLoadingUtils;

/**
 * Created by Will on 1/22/2016.
 */

public class NavigationFragment extends Fragment {
    private static final String TAG = "Navigation";
    private static MapView currentMapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        Button navigationAddressButton = (Button) view.findViewById(R.id.navigation_address_button);
        final LinearLayout addressBar = (LinearLayout) view.findViewById(R.id.navigationAddressBar);
        final EditText addressTextBox = (EditText) view.findViewById(R.id.navigation_address);
        final MapView mapView = (MapView) view.findViewById(R.id.navigationMapView);
        currentMapView = mapView;

        navigationAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationSearchButton(v, addressBar, addressTextBox, mapView);
            }
        });

        return view;
    }

    public void navigationSearchButton(View view, LinearLayout navAddressBar, EditText addressTextBox, MapView mapView) {
        Log.i(TAG, "navigationSearchButton() called");

        String address = addressTextBox.getText().toString();
        Address location = getAddressObjFromAddress(getActivity().getApplicationContext(), address);
        LatLng latAndLng = null;

        try {
            latAndLng = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        if (latAndLng == null)
        {
            return;
        }

        navAddressBar.setVisibility(View.INVISIBLE);
        mapView.setVisibility(View.VISIBLE);

        mapView.setCenter(latAndLng);
        mapView.setZoom(16);

        LatLng userLoc = mapView.getUserLocation();

        // On emulators, this will return null since there is no way to get the user location.
        // So, if userLoc is null, give it this hardcoded location (UC's campus).
        if (userLoc == null) {
            userLoc = new LatLng(39.131080, -84.517784);
        }


        String title = location.getAddressLine(0);
        String details = location.getLocality() + ", " + location.getAdminArea() + ", " + location.getCountryName();
        Marker marker = new Marker(title, details, latAndLng);
        marker.setToolTip(new NavigationInfoWindow(mapView, title, details,
                latAndLng.getLatitude(), latAndLng.getLongitude(), userLoc.getLatitude(), userLoc.getLongitude()));
        mapView.addMarker(marker);

        mapView.setUserLocationEnabled(true);
        mapView.setUserLocationTrackingMode(UserLocationOverlay.TrackingMode.FOLLOW);
        mapView.setUserLocationRequiredZoom(10);

        Marker userLocMarker = new Marker("Current Location", "", userLoc);
        mapView.addMarker(userLocMarker);


    }

    public Address getAddressObjFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        Address location = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            location = address.get(0);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return location;
    }
}
