package net.cyclestreets;

import com.nutiteq.android.MapView;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.location.LocationMarker;
import com.nutiteq.location.LocationSource;
import com.nutiteq.location.NutiteqLocationMarker;
import com.nutiteq.location.providers.AndroidGPSProvider;
import com.nutiteq.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

public class PlanRouteActivity extends Activity {
	protected MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// build layout for planroute tab

        // create MapView
        mapView = new MapView(this, CycleStreets.mapComponent);

//	    // create OpenStreetMapView
//        osmview = new OpenStreetMapView(this);
	    
        // create ZoomControls
        ZoomControls zoomControls = new ZoomControls(this);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
        	public void onClick(final View v) {
//        		osmview.getController().zoomIn();
        		CycleStreets.mapComponent.zoomIn();
        	}
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
        	public void onClick(final View v) {
//        		osmview.getController().zoomOut();
        		CycleStreets.mapComponent.zoomOut();
        	}
        });

        // GPS Location
        LocationSource locationSource = new AndroidGPSProvider(
        		(LocationManager) getSystemService(Context.LOCATION_SERVICE), 1000L);
        LocationMarker marker = new NutiteqLocationMarker(new PlaceIcon(Utils
        		.createImage("/res/drawable-mdpi/icon.png"), 5, 17), 3000, true);
        locationSource.setLocationMarker(marker);
        CycleStreets.mapComponent.setLocationSource(locationSource);	

        // listen for clicks
//        mapComponent.setMapListener(new MapListener() {
//        	 public void mapClicked(WgsPoint p) {
//        		 Log.d(getClass().getSimpleName(), "clicked at " + p.toString());
//        	 }
//        	 public void mapMoved() {}
//        	 public void needRepaint(boolean mapIsComplete) {}
//        });
        
        // add to planroute layout
        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
//        relativeLayout.addView(osmview, mapViewLayoutParams);
        relativeLayout.addView(mapView, mapViewLayoutParams);

        // add Zoom controls to the RelativeLayout
        RelativeLayout.LayoutParams zoomControlsLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        zoomControlsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        zoomControlsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relativeLayout.addView(zoomControls, zoomControlsLayoutParams);
        
    	setContentView(relativeLayout);
	}
	
    @Override
    protected void onDestroy() {
  	  super.onDestroy();
  	  if (mapView != null) {
  	      mapView.clean();
  	      mapView = null;
  	    }
  	}

	@Override
	protected void onResume() {
		super.onResume();
		// stop showing photos, if any
		CycleStreets.mapComponent.removeAllPlaces();
	}
}
