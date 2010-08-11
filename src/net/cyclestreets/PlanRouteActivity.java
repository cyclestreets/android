package net.cyclestreets;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.location.LocationMarker;
import com.nutiteq.location.LocationSource;
import com.nutiteq.location.NutiteqLocationMarker;
import com.nutiteq.location.providers.AndroidGPSProvider;
import com.nutiteq.maps.CloudMade;
import com.nutiteq.ui.ThreadDrivenPanning;
import com.nutiteq.utils.Utils;

public class PlanRouteActivity extends Activity {
    private MapView mapView;
    private BasicMapComponent mapComponent;
    private ZoomControls zoomControls;
    private boolean onRetainCalled;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
//        setContentView(R.layout.planroute_activity);
        onRetainCalled = false;

        mapComponent = new BasicMapComponent("c7e1249ffc03eb9ded908c236bd1996d4c62dbae56a439.28554625", "CycleStreets", "CycleStreets", 1, 1,
        		new WgsPoint(24.764580, 59.437420), 10);

        mapComponent.setMap(new CloudMade("13ed67dfecf44b5a8d9dc3ec49268ba0", "DEVICE_UID", 64, 1));
        mapComponent.setPanningStrategy(new ThreadDrivenPanning());
        mapComponent.setControlKeysHandler(new AndroidKeysHandler());
        mapComponent.startMapping();
        mapView = new MapView(this, mapComponent);

        //Add ZoomControls
        zoomControls = new ZoomControls(this);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
        	public void onClick(final View v) {
        		mapComponent.zoomIn();
        	}
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
        	public void onClick(final View v) {
        		mapComponent.zoomOut();
        	}
        });

        // GPS Location
        final LocationSource locationSource = new AndroidGPSProvider(
        		(LocationManager) getSystemService(Context.LOCATION_SERVICE), 1000L);
        final LocationMarker marker = new NutiteqLocationMarker(new PlaceIcon(Utils
        		.createImage("/res/drawable/icon.png"), 5, 17), 3000, true);
        locationSource.setLocationMarker(marker);
        mapComponent.setLocationSource(locationSource);

        final RelativeLayout relativeLayout = new RelativeLayout(this);
        setContentView(relativeLayout);
        final RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        relativeLayout.addView(mapView, mapViewLayoutParams);

        //Add Zoom controls View to the RelativeLayout
        final RelativeLayout.LayoutParams zoomControlsLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        zoomControlsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        zoomControlsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relativeLayout.addView(zoomControls, zoomControlsLayoutParams);  
//
//        
//        final RelativeLayout rl = new RelativeLayout(this);
//        
//        OpenStreetMapView mOsmv = new OpenStreetMapView(this, OpenStreetMapRendererInfo.getDefault());
//        mOsmv.setBuiltInZoomControls(true);
//        mOsmv.setMultiTouchControls(true);
//        rl.addView(mOsmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//        
//        this.setContentView(rl);
    }

//    protected boolean isRouteDisplayed() {
//    	return false;
//    }

    @Override
    public Object onRetainNonConfigurationInstance() {
      onRetainCalled = true;
      return mapComponent;
    }

    @Override
    protected void onDestroy() {
  	  super.onDestroy();
  	  if (mapView != null) {
  	      mapView.clean();
  	      mapView = null;
  	    }
  	  if (!onRetainCalled) {
  	      mapComponent.stopMapping();
  	      mapComponent = null;
  	    }
  	}
}
