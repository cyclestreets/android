package net.cyclestreets;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TabHost;

public class CycleStreets extends MainTabbedActivity
{
	public void onCreate(final Bundle savedInstanceState)	{
    MainSupport.switchMapFile(getIntent());
    super.onCreate(savedInstanceState);

    if (MainSupport.loadRoute(getIntent(), this))
      clearSavedTab();
  } // onCreate

  protected void addTabs(final TabHost tabHost) {
    addTab("Route Map", R.drawable.ic_tab_planroute, RouteMapFragment.class);
    addTab("Itinerary", R.drawable.ic_tab_itinerary, ItineraryFragment.class);
    addTab("Photomap", R.drawable.ic_tab_photomap, PhotoMapFragment.class);
    addTab("More ...", R.drawable.ic_tab_more, MoreFragment.class);
  } // addTabs

  private void clearSavedTab() {
    final SharedPreferences.Editor edit = prefs().edit();
    edit.putString("TAB", "");
    edit.commit();
  } // clearSavedTab

	public void showMap()	{
		setCurrentTab(0);
	} // showMap
} // class CycleStreets
