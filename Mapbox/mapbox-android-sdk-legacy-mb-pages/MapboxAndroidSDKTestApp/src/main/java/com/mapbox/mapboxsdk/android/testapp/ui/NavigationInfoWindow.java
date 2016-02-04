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

    private MapView mapView;
    private Double startLat;
    private Double startLong;
    private Double endLat;
    private Double endLong;

    public NavigationInfoWindow(MapView mv, String title, String details,
                                final Double startLatitude, final Double startLongitude, final Double endLatitude, final Double endLongitude) {
        super(R.layout.infowindow_navigation, mv);

        mapView = mv;
        startLat = startLatitude;
        startLong = startLongitude;
        endLat = endLatitude;
        endLong = endLongitude;

        TextView tvTitle = (TextView) mView.findViewById(R.id.navigation_title);
        TextView tvDetails = (TextView) mView.findViewById(R.id.navigation_details);

        tvTitle.setText(title);
        tvDetails.setText(details);

        TextView link = (TextView) mView.findViewById(R.id.navigation_link);
        LinearLayout container = (LinearLayout) mView.findViewById(R.id.navigation_container);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("", "onClick() called");
                OnNavigationLinkClicked(startLatitude, startLongitude, endLatitude, endLongitude);
                return;
            }
        });
    }

    public void OnNavigateTextClick(View v) {
        Log.i("", "OnNavigateTextClick() called");
        OnNavigationLinkClicked(startLat, startLong, endLat, endLong);
    }

    public void OnNavigationLinkClicked(Double startLatitude, Double startLongitude, Double endLatitude, Double endLongitude) {
        String accessToken = "pk.eyJ1IjoiYmxlZWdlIiwiYSI6IkRGLTFPU00ifQ.qJpq3jytAL9A-z_tkNypqg";
        String urlToGetRoutes = "https://api.mapbox.com/v4/directions/mapbox.driving/" + startLatitude.toString() + "," + startLongitude.toString() + ";" +
                endLatitude.toString() + "," + endLongitude.toString() + ".json?access_token=" + accessToken;

        RouteOverlayer overlayer = new RouteOverlayer();
        overlayer.doInBackground(urlToGetRoutes);
    }

    private class RouteOverlayer extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... url) {
            Log.i("", "doInBackground() called");
            try {
                JSONObject routesJsonObj = DataLoadingUtils.loadJSONFromUrl(url[0]);
                JSONArray routesJsonArray = routesJsonObj.getJSONArray("routes");
                return routesJsonArray;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray jsonRoutes) {
            Log.i("", "onPostExecute() called");
            if (jsonRoutes == null) {
                return;
            } else {
                try {
                    JSONObject firstRouteAsJson = jsonRoutes.getJSONObject(0);
                    LineString firstRouteAsLineString = (LineString) GeoJSON.parse(firstRouteAsJson);
                    List<Position> firstRoutAsPositions = firstRouteAsLineString.getPositions();
                    ArrayList<LatLng> firstRouteAsLatLngs = new ArrayList();
                    for (Position pos : firstRoutAsPositions) {
                        firstRouteAsLatLngs.add(new LatLng(pos.getLatitude(), pos.getLongitude()));
                    }

                    PathOverlay po = new PathOverlay();
                    po.addPoints(firstRouteAsLatLngs);
                    mapView.addOverlay(po);

                } catch (Exception ex) {

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
