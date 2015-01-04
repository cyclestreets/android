package net.cyclestreets;

import android.os.Bundle;

import net.cyclestreets.ElevationProfileFragment;
import net.cyclestreets.ItineraryFragment;
import net.cyclestreets.MainNavDrawerActivity;
import net.cyclestreets.MainSupport;
import net.cyclestreets.PhotoMapFragment;
import net.cyclestreets.PhotoUploadFragment;
import net.cyclestreets.RouteAvailablePageStatus;
import net.cyclestreets.RouteMapActivity;
import net.cyclestreets.RouteMapFragment;
import net.cyclestreets.WebPageFragment;

public class CycleStreets extends MainNavDrawerActivity
                          implements RouteMapActivity {
  @Override
  public void onCreate(final Bundle savedInstanceState)	{
    MainSupport.switchMapFile(getIntent());

    super.onCreate(savedInstanceState);

    MainSupport.loadRoute(getIntent(), this);
  } // onCreate

  @Override
  public void showMap() {
    showPage(0);
  } // showMap

  @Override
  protected void addDrawerItems() {
    addDrawerFragment(R.string.route_map,
                      R.drawable.ic_menu_mapmode,
                      RouteMapFragment.class);
    addDrawerFragment(R.string.itinerary,
                      R.drawable.ic_menu_agenda,
                      ItineraryAndElevationFragment.class,
                      new RouteAvailablePageStatus());
    addDrawerFragment(R.string.photomap,
                      R.drawable.ic_menu_gallery,
                      PhotoMapFragment.class);
    addDrawerFragment(R.string.photo_upload,
                      R.drawable.ic_menu_camera,
                      PhotoUploadFragment.class);
    addDrawerFragment(R.string.cyclestreets_blog,
                      -1,
                      WebPageFragment.class,
                      WebPageFragment.initialiser("http://www.cyclestreets.net/blog/"));
    addDrawerActivity(R.string.settings,
                      android.R.drawable.ic_menu_preferences,
                      SettingsActivity.class);
  } // addDrawerItems
} // CycleStreets
