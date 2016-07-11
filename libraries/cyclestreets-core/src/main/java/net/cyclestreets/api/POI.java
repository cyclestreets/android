package net.cyclestreets.api;

import android.graphics.drawable.Drawable;

import org.osmdroid.util.GeoPoint;

public class POI
{
  private final int id_;
  private final String name_;
  private final String notes_;
  private final String url_;
  private final GeoPoint pos_;
  
  private POICategory category_;
  
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
  
  void setCategory(final POICategory category) { category_ = category; }
  
  public int id() { return id_; }
  public String name() { return sOrNull(name_); }
  public String notes() { return sOrNull(notes_); }
  public String url() { return sOrNull(url_); }
  public GeoPoint position() { return pos_; }

  private String sOrNull(final String s) {
    return s != null ? s : "";
  } // sOrNull
  
  public POICategory category() { return category_; }
  public Drawable icon() { return category_.icon(); }
} // class POI
