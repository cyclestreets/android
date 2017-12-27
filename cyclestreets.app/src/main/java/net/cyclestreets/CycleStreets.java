package net.cyclestreets;

import android.os.Bundle;

public class CycleStreets extends MainNavDrawerActivity
                          implements RouteMapActivity {
  @Override
  public void onCreate(final Bundle savedInstanceState)	{
    MainSupport.switchMapFile(getIntent());

    super.onCreate(savedInstanceState);

    MainSupport.loadRoute(getIntent(), this);
  } // onCreate

  @Override
  protected void onFirstRun() {
    Welcome.welcome(this);
  }

  @Override
  protected void onNewVersion() {
    Welcome.whatsNew(this);
  } // onFirstRun

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
    addDrawerFragment(BlogFragment.blogTitle(this),
                      -1,
                      BlogFragment.class);
    addDrawerActivity(R.string.settings,
                      android.R.drawable.ic_menu_preferences,
                      SettingsActivity.class);
  } // addDrawerItems
} // CycleStreets
