package net.cyclestreets.api;

import org.osmdroid.util.GeoPoint;

public class POI
{
  private int id_;
  private String name_;
  private String notes_;
  private String url_;
  private GeoPoint pos_;
  
  public POI(final int id,
             final String name,
             final String notes,
             final String url,
             final double lat,
             final double lon)
  {
    id_ = id;
    name_ = name;
    notes_ = notes;
    url_ = url;
    pos_ = new GeoPoint(lat, lon);
  } // POI
  
  public int id() { return id_; }
  public String name() { return name_; }
  public String notes() { return notes_; }
  public String url() { return url_; }
  public GeoPoint position() { return pos_; }
} // class POI
