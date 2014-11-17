package net.cyclestreets.api;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class GeoPlace {
  private String name_;
  private String near_;
  private IGeoPoint coord_;
  
  public GeoPlace(final int latE6, final int longE6, final String name, final String near) {
    this(new GeoPoint(latE6/1E6, longE6/1E6), name, near);
  } // GeoPlace
  
  public GeoPlace(final IGeoPoint point, final String name, final String near) {
    coord_ = point;
    name_ = name;
    near_ = near;
  } // GeoPlace

  public String name() { return name_; }
  public String near() { return near_; }
  public IGeoPoint coord() { return coord_; }
  
  @Override
  public String toString() {
    String result = name_;
    if (near_ != null && near_.length() > 0) {
      if (name_.length() > 0)
        result = name_ + ", ";
      result += near_;
    }
    return result;
  } // toString
} // class GeoPlace
