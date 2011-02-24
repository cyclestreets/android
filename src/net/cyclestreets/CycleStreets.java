package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TabHost;

public class CycleStreets extends TabActivity {
	public static ApiClient apiClient = new ApiClient();
	
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

        // initialize objects
	    final Resources res = getResources();
	    final TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    
	    // Plan route
	    spec = tabHost.newTabSpec("Plan route").setIndicator("", res.getDrawable(R.drawable.ic_tab_planroute));
	    spec.setContent(new Intent(this, RouteMapActivity.class));
	    tabHost.addTab(spec);

	    // Itinerary
	    spec = tabHost.newTabSpec("Itinerary").setIndicator("", res.getDrawable(R.drawable.ic_tab_itinerary));
	    spec.setContent(new Intent(this, ItineraryActivity.class));
	    tabHost.addTab(spec);

	    // Photomap
	    spec = tabHost.newTabSpec("Photomap").setIndicator("", res.getDrawable(R.drawable.ic_tab_photomap));
	    spec.setContent(new Intent(this, PhotomapActivity.class));
	    tabHost.addTab(spec);

	    // Add photo
	    spec = tabHost.newTabSpec("Add photo").setIndicator("", res.getDrawable(R.drawable.ic_tab_addphoto));
	    spec.setContent(new Intent(this, AddPhotoActivity.class));
	    tabHost.addTab(spec);

	    spec = tabHost.newTabSpec("More").setIndicator("", res.getDrawable(R.drawable.ic_menu_info_details));
	    spec.setContent(new Intent(this, MoreActivity.class));
	    tabHost.addTab(spec);
	    
	    for(int i = 0; i != 5; ++i)
	    {
	    	final ViewGroup.LayoutParams layout = tabHost.getTabWidget().getChildAt(i).getLayoutParams();
		    layout.height = (int)(layout.height*0.66);
		    tabHost.getTabWidget().getChildAt(i).setLayoutParams(layout);
	    } // for ...
	    
	    // start with route tab
	    showMap();
	} // onCreate
	
	public void showMap()
	{
		getTabHost().setCurrentTab(0);
	} // showMap
	
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
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
} // class CycleStreets

