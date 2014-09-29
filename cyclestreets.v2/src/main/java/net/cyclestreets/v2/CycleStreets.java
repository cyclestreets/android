package net.cyclestreets.v2;

import net.cyclestreets.ItineraryFragment;
import net.cyclestreets.MainNavDrawerActivity;
import net.cyclestreets.MoreFragment;
import net.cyclestreets.PhotoMapFragment;
import net.cyclestreets.RouteMapFragment;

public class CycleStreets extends MainNavDrawerActivity {
  @Override
  protected void addPages() {
    addPage("Route Map", RouteMapFragment.class);
    addPage("Itinerary", ItineraryFragment.class);
    addPage("Photo Map", PhotoMapFragment.class);
    addPage("More ...", MoreFragment.class);
  } // addPages
} // CycleStreets
