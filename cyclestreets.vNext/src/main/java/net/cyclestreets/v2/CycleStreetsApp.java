package net.cyclestreets.v2;

import android.app.Application;

import net.cyclestreets.CycleStreetsAppSupport;
import net.cyclestreets.RegularUpdates;

public class CycleStreetsApp extends Application {
  @Override
  public void onCreate() {
    CycleStreetsAppSupport.initialise(this);

    RegularUpdates.schedule(this);
  } // onCreate
} // CycleStreetsApp
