package net.cyclestreets;

import android.app.Application;

import net.cyclestreets.CycleStreetsAppSupport;
import net.cyclestreets.RegularUpdates;

public class CycleStreetsApp extends Application {
  @Override
  public void onCreate() {
    CycleStreetsAppSupport.initialise(this, R.xml.prefs);

    RegularUpdates.schedule(this);
  } // onCreate
} // CycleStreetsApp
