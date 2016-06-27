package net.cyclestreets.api.client.dto;

import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.GeoPoint;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "result")
public class GeoPlaceDto {

  @Element
  private String name;
  @Element
  private String near;
  @Element
  private String type;
  @Element
  private double longitude;
  @Element
  private double latitude;

  public GeoPlace toGeoPlace() {
    return new GeoPlace(new GeoPoint(latitude, longitude), name, near);
  }
}