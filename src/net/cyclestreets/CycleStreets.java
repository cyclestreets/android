package net.cyclestreets;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;
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


public class CycleStreets extends TabActivity {
    protected MapView mapView;
    protected BasicMapComponent mapComponent;
    protected boolean onRetainCalled;
    
    protected final static String NUTITEQ_API_KEY = "c7e1249ffc03eb9ded908c236bd1996d4c62dbae56a439.28554625";
    protected final static String CLOUDMADE_API_KEY = "13ed67dfecf44b5a8d9dc3ec49268ba0";

    
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    // initialize default preferences
	    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	    
        // build layout for planroute tab
        buildPlanRouteLayout();

        // initialize objects
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    
	    // Plan route
	    spec = tabHost.newTabSpec("Plan route").setIndicator("Plan Route", res.getDrawable(R.drawable.ic_tab_planroute));
	    spec.setContent(R.id.tab_planroute);
	    tabHost.addTab(spec);

	    // Itinerary
	    spec = tabHost.newTabSpec("Itinerary").setIndicator("Itinerary", res.getDrawable(R.drawable.ic_tab_itinerary));
	    spec.setContent(new Intent(this, ItineraryActivity.class));
	    tabHost.addTab(spec);

	    // Photomap
	    spec = tabHost.newTabSpec("Photomap").setIndicator("Photomap", res.getDrawable(R.drawable.ic_tab_photomap));
	    spec.setContent(R.id.tab_photomap);
	    tabHost.addTab(spec);

	    // Add photo
	    spec = tabHost.newTabSpec("Add photo").setIndicator("Add photo", res.getDrawable(R.drawable.ic_tab_addphoto));
	    spec.setContent(R.id.tab_addphoto);
	    tabHost.addTab(spec);

	    // start with first tab
	    tabHost.setCurrentTab(0);
	}

	
	// build layout for planroute tab
	public void buildPlanRouteLayout() {
        onRetainCalled = false;

        // create MapComponent and MapView
        mapComponent = new BasicMapComponent(NUTITEQ_API_KEY, "CycleStreets", "CycleStreets", 1, 1,
        		new WgsPoint(24.764580, 59.437420), 10);
        String imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        mapComponent.setMap(new CloudMade(CLOUDMADE_API_KEY, imei, 64, 1));
        mapComponent.setPanningStrategy(new ThreadDrivenPanning());
        mapComponent.setControlKeysHandler(new AndroidKeysHandler());
        mapComponent.startMapping();
        mapView = new MapView(this, mapComponent);

        // create ZoomControls
        ZoomControls zoomControls = new ZoomControls(this);
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
        LocationSource locationSource = new AndroidGPSProvider(
        		(LocationManager) getSystemService(Context.LOCATION_SERVICE), 1000L);
        LocationMarker marker = new NutiteqLocationMarker(new PlaceIcon(Utils
        		.createImage("/res/drawable/icon.png"), 5, 17), 3000, true);
        locationSource.setLocationMarker(marker);
        mapComponent.setLocationSource(locationSource);	

        // add to planroute layout
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.tab_planroute);
        RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        relativeLayout.addView(mapView, mapViewLayoutParams);

        // add Zoom controls to the RelativeLayout
        RelativeLayout.LayoutParams zoomControlsLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        zoomControlsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        zoomControlsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relativeLayout.addView(zoomControls, zoomControlsLayoutParams);  
	}
	
	@Override
	/** build options menu */
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.toplevel_menu, menu);
	    return true;
	}

	@Override
	/** listener for menu selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.savedroutes:
	        // not yet
	        return true;
	    case R.id.settings:
	        startActivity(new Intent(this, SettingsActivity.class));
	        return true;
	    case R.id.credits:
	    	// not yet
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

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
