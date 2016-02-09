package com.mapbox.mapboxsdk.android.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.Position;
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
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

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
    /*  TODO:
        Figure out why you need to hit search twice.
        Fix the custom marker layout after you've overlayed a map.
        Test!
     */

    private static final String TAG = "Navigation";
    private MapView currentMapView;
    private View currentView;

    public boolean skipSearchBar = false;
    public LineString routeToDisplay = null;
    public LatLng initialLatAndLng = null;

    private Marker cachedSearchedAddressMarker;
    private Marker cachedCurrentLocationMarker;
    private NavigationInfoWindow cachedMarkerStyle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);


        // Find all the UI elements
        Button navigationAddressButton = (Button) view.findViewById(R.id.navigation_address_button);
        final LinearLayout addressBar = (LinearLayout) view.findViewById(R.id.navigationAddressBar);
        final EditText addressTextBox = (EditText) view.findViewById(R.id.navigation_address);
        final MapView mapView = (MapView) view.findViewById(R.id.navigationMapView);

        currentMapView = mapView;
        currentView = view;

        // Check if we should just go the map
        if (skipSearchBar) {
            if (initialLatAndLng == null) {
                // Set to default (UC's campus)
                initialLatAndLng = new LatLng(39.131080, -84.517784);
            }

            // Set the screen to show the mapview
            PrepUIToShowMap(initialLatAndLng, addressBar, mapView);

            // Add cached markers
            if (cachedSearchedAddressMarker != null) {
                if (cachedMarkerStyle != null) {
                    cachedSearchedAddressMarker.setToolTip(cachedMarkerStyle);
                }
                mapView.addMarker(cachedSearchedAddressMarker);
            }

            if (cachedCurrentLocationMarker != null) {
                mapView.addMarker(cachedCurrentLocationMarker);
            }

            if (routeToDisplay != null) {
                OverlayRouteFromGeoJsonLineString(routeToDisplay);
            }
        } else {

            // Set the listener for clicking "search"
            navigationAddressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationSearchButton(v, addressBar, addressTextBox, mapView);
                }
            });
        }



        return view;
    }

    public void navigationSearchButton(View view, LinearLayout navAddressBar, EditText addressTextBox, MapView mapView) {
        Log.i(TAG, "navigationSearchButton() called");

        // Close the keyboard
        InputMethodManager imm  = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // Get the address and corresponding LatLng
        String address = addressTextBox.getText().toString();
        Address location = getAddressObjFromAddress(getActivity().getApplicationContext(), address);
        LatLng latAndLng = null;
        try {
            latAndLng = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (Exception ex) {
            Toast.makeText(view.getContext(), "Could not find location.", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            return;
        }

        // If it's null, do nothing
        if (latAndLng == null)
        {
            Toast.makeText(view.getContext(), "Could not find location.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the UI visibility
        PrepUIToShowMap(latAndLng, navAddressBar, mapView);

        // Get the user location
        mapView.setUserLocationEnabled(true);
        mapView.setUserLocationTrackingMode(UserLocationOverlay.TrackingMode.FOLLOW);
        mapView.setUserLocationRequiredZoom(10);
        LatLng userLoc = mapView.getUserLocation();

        // On emulators, this will return null since there is no way to get the user location.
        // So, if userLoc is null, give it this hardcoded location (UC's campus).
        if (userLoc == null) {
            userLoc = new LatLng(39.131080, -84.517784);
        }

        // Create a new marker for the entered address
        String title = location.getAddressLine(0);
        String details = location.getLocality() + ", " + location.getAdminArea() + ", " + location.getCountryName();
        Marker marker = new Marker(title, details, latAndLng);
        NavigationInfoWindow navInfoWindow = new NavigationInfoWindow(mapView, this, getFragmentManager(), title, details,
                latAndLng.getLatitude(), latAndLng.getLongitude(), userLoc.getLatitude(), userLoc.getLongitude());
        marker.setToolTip(navInfoWindow);
        mapView.addMarker(marker);

        // Create a new marker for the current user location
        Marker userLocMarker = new Marker("Current Location", "", userLoc);
        mapView.addMarker(userLocMarker);

        cachedCurrentLocationMarker = userLocMarker;
        cachedSearchedAddressMarker = marker;
        cachedMarkerStyle = navInfoWindow;
    }

    private void PrepUIToShowMap(LatLng initialLocation, LinearLayout navAddressBar, MapView mapView) {
        navAddressBar.setVisibility(View.INVISIBLE);
        mapView.setVisibility(View.VISIBLE);

        // Orient the mapview
        mapView.setCenter(initialLocation);
        mapView.setZoom(16);
    }

    //private void DisplayMarkers

    /*
        Function adapted from code from stackoverflow.com.
    */
    private Address getAddressObjFromAddress(Context context, String strAddress) {
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

    /*
        Function adapted from code by John Mikolay and code from stackoverflow.com.
    */
    public void OverlayRouteFromGeoJsonLineString(LineString  routeAsGeoJsonLineString) {
        try {
            // Get the first json route (our primary route)
            //LineString firstRouteAsLineString = (LineString) GeoJSON.parse(new JSONObject(routeAsGeoJsonLineString));

            // Turn it into an array of LatLngs
            List<Position> routeAsPositions = routeAsGeoJsonLineString.getPositions();
            ArrayList<LatLng> routeAsLatLngs = new ArrayList();
            for (int i = 0; i < routeAsPositions.size(); i++) {
                routeAsLatLngs.add(new LatLng(routeAsPositions.get(i).getLatitude(), routeAsPositions.get(i).getLongitude()));
            }

            // Overlay it as a path
            PathOverlay po = new PathOverlay();
            po.addPoints(routeAsLatLngs);
            currentMapView.addOverlay(po);

        } catch (Exception ex) {
            String exMessage = ex.getMessage();
            Log.i(TAG, "Exception in OverlayRouteFromGeoJsonLineString()");
            Log.i(TAG, exMessage);
            Toast.makeText(currentView.getContext(), "Could not overlay route.", Toast.LENGTH_SHORT).show();
        }
    }
}
