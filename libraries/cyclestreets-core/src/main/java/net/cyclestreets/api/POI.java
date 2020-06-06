package net.cyclestreets.api;

import android.graphics.drawable.Drawable;

import org.osmdroid.util.GeoPoint;

public class POI
{
  private final int id;
  private final String name;
  private final String notes;
  private final String url;
  private final String phone;
  private final String openingHours;
  private final GeoPoint pos;

  private POICategory category;

  public POI(final int id,
             final String name,
             final String notes,
             final String url,
             final String phone,
             final String openingHours,
             final double lat,
             final double lon) {
    this.id = id;
    this.name = name;
    this.notes = notes;
    this.url = url;
    this.phone = phone;
    this.openingHours = openingHours;
    pos = new GeoPoint(lat, lon);
  }

  void setCategory(final POICategory category) { this.category = category; }

  public int id() { return id; }
  public String name() { return stringOrBlank(name); }
  public String notes() { return stringOrBlank(notes); }
  public String url() { return stringOrBlank(url); }
  public String phone() { return stringOrBlank(phone); }
  public String openingHours() { return stringOrBlank(openingHours); }
  public GeoPoint position() { return pos; }

  private String stringOrBlank(final String s) {
    return s != null ? s : "";
  }

  public POICategory category() { return category; }
  public Drawable icon() { return category.getIcon(); }
}
