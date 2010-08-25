package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.maps.CloudMade;
import com.nutiteq.ui.ThreadDrivenPanning;

public class CycleStreets extends TabActivity {
	protected static ApiClient apiClient = new ApiClient();
    protected static BasicMapComponent mapComponent;
//	protected static OpenStreetMapView osmview;
	protected boolean onRetainCalled;
    
    
    protected final static String NUTITEQ_API_KEY = "c7e1249ffc03eb9ded908c236bd1996d4c62dbae56a439.28554625";
    protected final static String CLOUDMADE_API_KEY = "13ed67dfecf44b5a8d9dc3ec49268ba0";

    protected final static WgsPoint CAMBRIDGE = new WgsPoint(-0.74483, 52.2099121);
    
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

        onRetainCalled = false;

        // initialize default preferences
	    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	    // initialize mapcomponent
        mapComponent = new BasicMapComponent(NUTITEQ_API_KEY, "CycleStreets", "CycleStreets", 1, 1,
        		CAMBRIDGE, 10);
        String imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        mapComponent.setMap(new CloudMade(CLOUDMADE_API_KEY, imei, 64, 1));
        mapComponent.setPanningStrategy(new ThreadDrivenPanning());
        mapComponent.setControlKeysHandler(new AndroidKeysHandler());
        mapComponent.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
        mapComponent.startMapping();

        // initialize objects
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    
	    // Plan route
	    spec = tabHost.newTabSpec("Plan route").setIndicator("Plan Route", res.getDrawable(R.drawable.ic_tab_planroute));
	    spec.setContent(new Intent(this, PlanRouteActivity.class));
	    tabHost.addTab(spec);

	    // Itinerary
	    spec = tabHost.newTabSpec("Itinerary").setIndicator("Itinerary", res.getDrawable(R.drawable.ic_tab_itinerary));
	    spec.setContent(new Intent(this, ItineraryActivity.class));
	    tabHost.addTab(spec);

	    // Photomap
	    spec = tabHost.newTabSpec("Photomap").setIndicator("Photomap", res.getDrawable(R.drawable.ic_tab_photomap));
	    spec.setContent(new Intent(this, PhotomapActivity.class));
	    tabHost.addTab(spec);

	    // Add photo
	    spec = tabHost.newTabSpec("Add photo").setIndicator("Add photo", res.getDrawable(R.drawable.ic_tab_addphoto));
	    spec.setContent(new Intent(this, AddPhotoActivity.class));
	    tabHost.addTab(spec);

	    // start with photomap tab
	    tabHost.setCurrentTab(2);
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
  	  if (!onRetainCalled) {
  	      mapComponent.stopMapping();
  	      mapComponent = null;
  	    }
  	}
}
