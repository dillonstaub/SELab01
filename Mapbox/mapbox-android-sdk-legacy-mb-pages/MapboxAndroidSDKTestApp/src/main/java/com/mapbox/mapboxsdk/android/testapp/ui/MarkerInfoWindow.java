package com.mapbox.mapboxsdk.android.testapp.ui;

import android.content.DialogInterface;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.android.testapp.R;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by Matt on 2/2/2016.
 */
public class MarkerInfoWindow extends InfoWindow {

    public MarkerInfoWindow(MapView mv) {
        super(R.layout.infowindow_marker, mv);

        // Add own OnTouchListener to customize handling InfoWindow touch events
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Demonstrate custom onTouch() control
                    Toast.makeText(mView.getContext(), R.string.customInfoWindowOnTouchMessage, Toast.LENGTH_SHORT).show();

                    // Still close the InfoWindow though
                    close();
                }

                // Return true as we're done processing this event
                return true;
            }
        });
    }

    /**
     * Dynamically set the content in the CustomInfoWindow
     * @param overlayItem The tapped Marker
     */
    @Override
    public void onOpen(Marker overlayItem) {
        String title = overlayItem.getTitle();
        String subdescription = overlayItem.getSubDescription();
        ((TextView) mView.findViewById(R.id.markerTooltip_title)).setText(title);
        ((TextView) mView.findViewById(R.id.markerTooltip_subdescription)).setText(subdescription);
    }
}