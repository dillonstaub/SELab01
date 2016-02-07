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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.LineString;
import com.mapbox.mapboxsdk.android.testapp.ui.NavigationInfoWindow;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import com.mapbox.mapboxsdk.android.testapp.NavigationFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 1/22/2016.
 */

public class NavigationRoutesFragment extends Fragment {
    private static final String TAG = "NavigationRoutes";

    private static final String ROUTES_PARAM = "routes";

    public static NavigationRoutesFragment createInstance(String routes) {
        NavigationRoutesFragment fragment = new NavigationRoutesFragment();
        Bundle args = new Bundle();
        args.putString(ROUTES_PARAM, routes);
        fragment.setArguments(args);
        return fragment;
    }

    public NavigationFragment navFragment;
    public JSONArray jsonRoutesArray;

    //private ArrayList<LineString> routesAsLineStrings;
    private ArrayList<JSONObject> routesAsJsonObjects;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigationroutes, container, false);

        navFragment = (NavigationFragment) getFragmentManager().findFragmentByTag(getString(R.string.navigationFragmentTag));
        try {
            //JSONObject obj = new JSONObject(getArguments().getString(ROUTES_PARAM));
            jsonRoutesArray = new JSONArray(getArguments().getString(ROUTES_PARAM));//obj.getJSONArray("routes");
        } catch (Exception ex) {
            Log.i(TAG, "Error getting json in NavRouteFrag.");
            Log.i(TAG, ex.getMessage());
        }

        // Add all the LineStrings for the routes to our private field
        routesAsJsonObjects = new ArrayList<JSONObject>();
        for (int ii = 0; ii < jsonRoutesArray.length(); ii++) {
            try {
                routesAsJsonObjects.add(jsonRoutesArray.getJSONObject(ii));
                //JSONObject routeAsJson = jsonRoutesArray.getJSONObject(ii);
                //routesAsLineStrings.add((LineString) GeoJSON.parse(routeAsJson.getJSONObject("geometry")));
            } catch (Exception ex) {
                String exMessage = ex.getMessage();
                Log.i(TAG, "Exception in onCreateView() for NavigatinRoutesFragment, iteration " + ii);
                Log.i(TAG, exMessage);
            }
        }

        // Configure the adapter
        ListView routesListView = (ListView) view.findViewById(R.id.navigation_routesList);
        routesListView.setAdapter(new RoutesListAdapter(getActivity().getApplicationContext(),
                R.layout.fragment_navigationrouteitem, routesAsJsonObjects));


        return view;
    }

    private void SwitchToNavigationFragment(LineString routeToDisplay) {
        navFragment.routeToDisplay = routeToDisplay;
        navFragment.skipSearchBar = true;
        getFragmentManager().popBackStackImmediate();
    }

    private class RoutesListAdapter extends ArrayAdapter<JSONObject> {
        private int resource;
        private RoutesListAdapter(Context context, int resource, List<JSONObject> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(resource, parent, false);
            RouteInformationObject routeInfoObj = new RouteInformationObject();

            // Get UI elements
            routeInfoObj.selectionButton = (Button) convertView.findViewById(R.id.navigation_selectionButton);
            routeInfoObj.routeId = (TextView) convertView.findViewById(R.id.navigation_routeId);
            routeInfoObj.routeDistanceInMiles = (TextView) convertView.findViewById(R.id.navigation_routeDistanceInMiles);
            final JSONObject thisRoute = getItem(position);

            // Configure button
            routeInfoObj.selectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject tempGeo = thisRoute.getJSONObject("geometry");
                        LineString tempLS = (LineString) GeoJSON.parse(tempGeo);
                        SwitchToNavigationFragment(tempLS);
                    } catch (Exception ex) {
                        Log.i(TAG, "Error in onClick for item in pos " + position + " in NavRouteFrag.");
                        Log.i(TAG, ex.getMessage());
                    }
                }
            });

            // Configure routeId
            routeInfoObj.routeId.setText("Route " + (position + 1));

            // Configure routeDistance
            try {
                double milesPerMeter = 0.000621371;
                double miles = thisRoute.getDouble("distance") * milesPerMeter;
                DecimalFormat decFormat = new DecimalFormat("#.#");
                routeInfoObj.routeDistanceInMiles.setText(decFormat.format(miles) + " miles");
            } catch (Exception ex) {
                routeInfoObj.routeDistanceInMiles.setText("Error");
                Log.i(TAG, "Error getting dist for item in pos " + position + " in NavRouteFrag.");
                Log.i(TAG, ex.getMessage());
            }

            return convertView;
        }
    }

    private class RouteInformationObject {
        Button selectionButton;
        TextView routeId;
        TextView routeDistanceInMiles;
    }

}
