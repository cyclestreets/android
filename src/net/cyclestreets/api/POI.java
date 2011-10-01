package net.cyclestreets.api;

import org.osmdroid.util.GeoPoint;

public class POI
{
  private String name_;
  private String notes_;
  private String url_;
  private GeoPoint pos_;
  
  public POI(final String name,
             final String notes,
             final String url,
             final double lat,
             final double lon)
  {
    name_ = name;
    notes_ = notes;
    url_ = url;
    pos_ = new GeoPoint(lat, lon);
  } // POI
  
  public String name() { return name_; }
  public String notes() { return notes_; }
  public String url() { return url_; }
  public GeoPoint position() { return pos_; }
} // class POI
