package net.cyclestreets.v2;

import android.os.Bundle;

import net.cyclestreets.ItineraryFragment;
import net.cyclestreets.MainNavDrawerActivity;
import net.cyclestreets.MainSupport;
import net.cyclestreets.MoreFragment;
import net.cyclestreets.PhotoMapFragment;
import net.cyclestreets.PhotoUploadFragment;
import net.cyclestreets.RouteMapFragment;
import net.cyclestreets.WebPageFragment;

public class CycleStreets extends MainNavDrawerActivity {
  public void onCreate(final Bundle savedInstanceState)	{
    MainSupport.switchMapFile(getIntent());

    super.onCreate(savedInstanceState);

    MainSupport.loadRoute(getIntent(), this);
  } // onCreate

  @Override
  protected void addPages() {
    addPage("Route Map", R.drawable.ic_menu_mapmode_white, RouteMapFragment.class);
    addPage("Itinerary", R.drawable.ic_menu_agenda_white, ItineraryFragment.class);
    addPage("Photomap", R.drawable.ic_menu_gallery_white, PhotoMapFragment.class);
    addPage("Photo Upload", R.drawable.ic_menu_camera_white, PhotoUploadFragment.class);
    addPage("Blog",
        -1,
        WebPageFragment.class,
        WebPageFragment.initialiser("http://cyclestreets.net/blog/"));

    addPage("More ...", R.drawable.ic_menu_info_details_white, MoreFragment.class);
  } // addPages
} // CycleStreets
