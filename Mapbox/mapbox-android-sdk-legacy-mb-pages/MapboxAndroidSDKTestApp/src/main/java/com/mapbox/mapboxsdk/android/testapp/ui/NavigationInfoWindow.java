package com.mapbox.mapboxsdk.android.testapp.ui;

import android.os.AsyncTask;
import android.provider.ContactsContract;
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


import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class NavigationInfoWindow extends InfoWindow {
    private static final String TAG = "Navigation";

    private MapView mapView;
    private Double startLat;
    private Double startLong;
    private Double endLat;
    private Double endLong;

    public NavigationInfoWindow(MapView mv, String title, String details,
                                final Double startLatitude, final Double startLongitude, final Double endLatitude, final Double endLongitude) {
        super(R.layout.infowindow_navigation, mv);

        // Assign our private members for later use
        mapView = mv;
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

        /*link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("", "onClick() called");
                OnNavigationLinkClicked(startLatitude, startLongitude, endLatitude, endLongitude);
                return;
            }
        });*/

        // Set the listener for clicking the marker
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onTouch() called");
                /*if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Demonstrate custom onTouch() control
                    Toast.makeText(mView.getContext(), R.string.customInfoWindowOnTouchMessage, Toast.LENGTH_SHORT).show();

                    // Still close the InfoWindow though
                    close();
                }*/

                OnNavigationLinkClicked(startLatitude, startLongitude, endLatitude, endLongitude);
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
                Log.i(TAG, ex.getMessage());
                return null;
            }

            /* thought... move the first few lines in onPostEx (the ones that parse the json) to here. */
        }

        @Override
        protected void onPostExecute(JSONObject jsonRoutes) {
            Log.i(TAG, "onPostExecute() called");
            JSONArray jsonRoutesArray = null;
            try {
                jsonRoutesArray = jsonRoutes.getJSONArray("routes");
            } catch (Exception ex) {
                Log.i(TAG, ex.getMessage());
                return;
            }

            if (jsonRoutesArray == null) {
                return;
            } else {
                try {
                    // Get the first json route (our primary route)
                    JSONObject firstRouteAsJson = jsonRoutesArray.getJSONObject(0);
                    LineString firstRouteAsLineString = (LineString) GeoJSON.parse(firstRouteAsJson.getJSONObject("geometry"));

                    // Turn it into an array of LatLngs
                    List<Position> firstRoutAsPositions = firstRouteAsLineString.getPositions();
                    ArrayList<LatLng> firstRouteAsLatLngs = new ArrayList();
                    for (int i = 0; i < firstRoutAsPositions.size(); i++) {
                        firstRouteAsLatLngs.add(new LatLng(firstRoutAsPositions.get(i).getLatitude(), firstRoutAsPositions.get(i).getLongitude()));
                    }

                    // Overlay it as a path
                    PathOverlay po = new PathOverlay();
                    po.addPoints(firstRouteAsLatLngs);
                    mapView.addOverlay(po);

                } catch (Exception ex) {
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
