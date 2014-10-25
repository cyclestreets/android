package net.cyclestreets.views;

import org.osmdroid.util.GeoPoint;

public class CycleMapDefaults {
  public static GeoPoint centre() { return centre_; }
  public static boolean gps() { return gps_; }


  public static void setCentre(GeoPoint gp) { centre_ = new GeoPoint(gp); }
  public static void setGps(boolean gps) { gps_ = gps; }

  private static GeoPoint centre_ = new GeoPoint(51477841, 0); /* Greenwich! */
  private static boolean gps_ = true;
} // class CycleMapDefaults
