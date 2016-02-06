package com.mapbox.mapboxsdk.android.testapp.ui;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Position;
import com.cocoahero.android.geojson.LineString;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.android.testapp.R;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import com.mapbox.mapboxsdk.android.testapp.NavigationFragment;
import com.mapbox.mapboxsdk.android.testapp.NavigationRoutesFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class NavigationInfoWindow extends InfoWindow {
    private static final String TAG = "NavigationInfoWindow";

    private MapView mapView;
    private NavigationFragment owningNavFragment;
    private FragmentManager fragManager;
    private Double startLat;
    private Double startLong;
    private Double endLat;
    private Double endLong;

    public NavigationInfoWindow(MapView mv, NavigationFragment owningFragment, FragmentManager fragmentManager, String title, String details,
                                final Double startLatitude, final Double startLongitude, final Double endLatitude, final Double endLongitude) {
        super(R.layout.infowindow_navigation, mv);

        // Assign our private members for later use
        mapView = mv;
        owningNavFragment = owningFragment;
        fragManager = fragmentManager;
        startLat = startLatitude;
        startLong = startLongitude;
        endLat = endLatitude;
        endLong = endLongitude;

        // Find and update the UI
        TextView tvTitle = (TextView) mView.findViewById(R.id.navigation_title);
        TextView tvDetails = (TextView) mView.findViewById(R.id.navigation_details);

        tvTitle.setText(title);
        tvDetails.setText(details);

        TextView link = (TextView) mView.findViewById(R.id.navigation_link);
        LinearLayout container = (LinearLayout) mView.findViewById(R.id.navigation_container);


        // Set the listener for clicking the marker
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onTouch() called");
                OnNavigationLinkClicked(startLatitude, startLongitude, endLatitude, endLongitude);
                close();
                return true;
            }
        });
    }

    public void OnNavigationLinkClicked(Double startLatitude, Double startLongitude, Double endLatitude, Double endLongitude) {
        Log.i(TAG, "OnNavigationLinkClicked() called");

        // Build the url
        String accessToken = "pk.eyJ1IjoiYmxlZWdlIiwiYSI6IkRGLTFPU00ifQ.qJpq3jytAL9A-z_tkNypqg";
        String urlToGetRoutes = "http://api.mapbox.com/v4/directions/mapbox.driving/" + startLongitude.toString() + "," + startLatitude.toString() + ";" +
                endLongitude.toString() + "," + endLatitude.toString() + ".json?access_token=" + accessToken;

        // Create and start a new RouteOverlayer
        RouteOverlayer overlayer = new RouteOverlayer();
        overlayer.execute(urlToGetRoutes);
        //overlayer.doInBackground(urlToGetRoutes);
    }

    private class RouteOverlayer extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... url) {
            Log.i(TAG, "doInBackground() called");
            try {
                // Retrieve and parse the json
                JSONObject routesJsonObj = DataLoadingUtils.loadJSONFromUrl(url[0]);
                return routesJsonObj;
            } catch (Exception ex) {
                Log.i(TAG, "Exception in doInBackground()");
                Log.i(TAG, ex.getMessage());
                return null;
            }

            /* thought... move the first few lines in onPostEx (the ones that parse the json) to here. */
        }

        @Override
        protected void onPostExecute(JSONObject jsonRoutes) {
            Log.i(TAG, "onPostExecute() called");

            // Parse the JSON
            JSONArray jsonRoutesArray = null;
            try {
                jsonRoutesArray = jsonRoutes.getJSONArray("routes");
            } catch (Exception ex) {
                Log.i(TAG, "Exception in onPostExecute() (1)");
                Log.i(TAG, ex.getMessage());
                Toast.makeText(mView.getContext(), "Could not get routes.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (jsonRoutesArray == null || jsonRoutesArray.length() == 0) {
                Toast.makeText(mView.getContext(), "Could not get routes.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    if (jsonRoutesArray.length() == 1) {
                        // Get the first json route (our primary route)
                        JSONObject firstRouteAsJson = jsonRoutesArray.getJSONObject(0);
                        LineString firstRouteAsLineString = (LineString) GeoJSON.parse(firstRouteAsJson.getJSONObject("geometry"));
                        owningNavFragment.OverlayRouteFromGeoJsonLineString(firstRouteAsLineString);
                    } else {
                        // Create the new navigation routes fragment and give it the navigation fragment
                        NavigationRoutesFragment newNavRoutesFrag = NavigationRoutesFragment.createInstance(jsonRoutesArray.toString());// new NavigationRoutesFragment();
                        newNavRoutesFrag.navFragment = owningNavFragment;
                        //newNavRoutesFrag.jsonRoutesArray = jsonRoutesArray;

                        FragmentTransaction transaction = fragManager.beginTransaction();
                        transaction.replace(R.id.content_frame, newNavRoutesFrag);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }

                } catch (Exception ex) {
                    Log.i(TAG, "Exception in onPostExecute() (2)");
                    String exMessage = ex.getMessage();
                    Log.i(TAG, exMessage);
                }
            }
        }
    }

    /**
     * Dynamically set the content in the CustomInfoWindow
     * @param overlayItem The tapped Marker
     */
    @Override
    public void onOpen(Marker overlayItem) {
        //String title = overlayItem.getTitle();
        //((TextView) mView.findViewById(R.id.customTooltip_title)).setText(title);
    }
}
