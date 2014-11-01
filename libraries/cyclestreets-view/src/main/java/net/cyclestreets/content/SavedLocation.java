package net.cyclestreets.content;

import org.osmdroid.util.GeoPoint;

public class SavedLocation {
  private int id_;
  private String name_;
  private GeoPoint where_;

  SavedLocation(final int id,
                final String name,
                final int whereLat,
                final int whereLon) {
    id_ = id;
    name_ = name;
    where_ = new GeoPoint(whereLat, whereLon);
  } // SavedLocation

  public int localId() { return id_; }
  public String name() { return name_; }
  public GeoPoint where() { return where_; }
} // SavedLocation
