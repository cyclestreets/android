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
                      R.drawable.ic_menu_mapmode_black,
                      RouteMapFragment.class);
    addDrawerFragment(R.string.itinerary,
                      R.drawable.ic_menu_agenda,
                      R.drawable.ic_menu_agenda_black,
                      ItineraryAndElevationFragment.class,
                      new RouteAvailablePageStatus());
    addDrawerFragment(R.string.photomap,
                      R.drawable.ic_menu_gallery,
                      R.drawable.ic_menu_gallery_black,
                      PhotoMapFragment.class);
    addDrawerFragment(R.string.photo_upload,
                      R.drawable.ic_menu_camera,
                      R.drawable.ic_menu_camera_black,
                      PhotoUploadFragment.class);
    addDrawerFragment(R.string.cyclestreets_blog,
                      -1,
                      -1,
                      WebPageFragment.class,
                      WebPageFragment.initialiser("http://www.cyclestreets.net/blog/"));

    addDrawerActivity(R.string.settings,
                      android.R.drawable.ic_menu_preferences,
                      SettingsActivity.class);
  } // addDrawerItems
} // CycleStreets
