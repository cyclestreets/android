package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Journey;
import uk.org.invisibility.cycloid.MapActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import org.osmdroid.util.GeoPoint;

public class CycleStreets extends TabActivity {
	public static ApiClient apiClient = new ApiClient();
	private static Journey journey_;
	private static GeoPoint from_;
	private static GeoPoint to_;
	
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

        // initialize default preferences
	    CycleStreetsPreferences.initialise(this);

        // initialize objects
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    
	    // Plan route
	    spec = tabHost.newTabSpec("Plan route").setIndicator("Plan Route", res.getDrawable(R.drawable.ic_tab_planroute));
	    spec.setContent(new Intent(this, MapActivity.class));
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

	    // start with route tab
	    tabHost.setCurrentTab(0);
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
	
	//////////////////////////////////////////////////
	static public Journey journey() { return journey_; }
	static public GeoPoint from() { return from_; }
	static public GeoPoint to() { return to_; }
	
	static public void onNewJourney(final Journey journey, final GeoPoint from, final GeoPoint to)
	{
		journey_ = journey;
		from_ = from;
		to_ = to;
	} // onNewJourney
} // class CycleStreets

