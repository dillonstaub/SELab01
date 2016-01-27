package com.mapbox.mapboxsdk.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

/**
 * Created by Will on 1/22/2016.
 */

public class NavigationFragment extends Fragment {
    private static final String TAG = "Navigation";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        Button navigationAddressButton = (Button) view.findViewById(R.id.navigation_address_button);
        final LinearLayout addressBar = (LinearLayout) view.findViewById(R.id.navigationAddressBar);
        final MapView mapView = (MapView) view.findViewById(R.id.navigationMapView);


        navigationAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationSearchButton(v, addressBar, mapView);
            }
        });

        return view;
    }

    public void navigationSearchButton(View view, LinearLayout navAddressBar, MapView mapView) {
        Log.i(TAG, "navigationSearchButton() called");

        /*MapView mv = (MapView) view.findViewById(R.id.navigationMapView);
        mv.setCenter(new LatLng(-3.07881, 37.31369));
        mv.setZoom(8);

        Marker marker = new Marker("Mount Kilimanjaro", "", new LatLng(-3.06372, 36.71356));
        marker.setMarker(getResources().getDrawable(R.drawable.right_arrow));
        mv.addMarker(marker);*/

        //LinearLayout navAddressBar = (LinearLayout)view.findViewById(R.id.navigationAddressBar);

        navAddressBar.setVisibility(View.INVISIBLE);

        mapView.setVisibility(View.VISIBLE);

        String address = "1330 Lower Bellbrook Rd";

        LatLng latAndLng = getLocationFromAddress(getActivity().getApplicationContext(), address);

        mapView.setCenter(latAndLng);
        mapView.setZoom(8);

        Marker marker = new Marker("" + latAndLng.getLatitude(), "" + latAndLng.getLongitude(), latAndLng);
        //marker.setMarker(getResources().getDrawable(R.drawable.right_arrow));
        mapView.addMarker(marker);
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return p1;
    }
}
