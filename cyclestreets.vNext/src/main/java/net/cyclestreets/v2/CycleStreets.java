package net.cyclestreets.v2;

import android.os.Bundle;

import net.cyclestreets.ItineraryFragment;
import net.cyclestreets.MainNavDrawerActivity;
import net.cyclestreets.MainSupport;
import net.cyclestreets.MoreFragment;
import net.cyclestreets.PhotoMapFragment;
import net.cyclestreets.RouteMapFragment;

public class CycleStreets extends MainNavDrawerActivity {
  public void onCreate(final Bundle savedInstanceState)	{
    MainSupport.switchMapFile(getIntent());

    super.onCreate(savedInstanceState);

    MainSupport.loadRoute(getIntent(), this);
  } // onCreate

  @Override
  protected void addPages() {
    addPage("Route Map", RouteMapFragment.class);
    addPage("Itinerary", ItineraryFragment.class);
    addPage("Photo Map", PhotoMapFragment.class);
    addPage("More ...", MoreFragment.class);
  } // addPages
} // CycleStreets
