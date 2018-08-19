package net.cyclestreets;

import android.app.Application;

public class CycleStreetsApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    CycleStreetsAppSupport.initialise(this, R.xml.prefs);
  }
}
