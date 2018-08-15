package net.cyclestreets;

import android.os.Bundle;

import net.cyclestreets.addphoto.AddPhotoFragment;

public class CycleStreets extends MainNavDrawerActivity implements RouteMapActivity {
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    MainSupport.switchMapFile(getIntent());

    super.onCreate(savedInstanceState);

    MainSupport.loadRoute(getIntent(), this);
  }

  @Override
  protected void onFirstRun() {
    Welcome.welcome(this);
  }

  @Override
  protected void onNewVersion() {
    Welcome.whatsNew(this);
  }

  @Override
  public void showMap() {
    showPage(0);
  }

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
                      AddPhotoFragment.class);
    addDrawerFragment(BlogFragment.blogTitle(this),
                      -1,
                      BlogFragment.class);
    addDrawerFragment(R.string.settings,
                      android.R.drawable.ic_menu_preferences,
                      SettingsFragment.class);
  }
}
