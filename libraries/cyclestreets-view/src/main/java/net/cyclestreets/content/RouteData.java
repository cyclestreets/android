package net.cyclestreets.content;

import net.cyclestreets.routing.Waypoints;

public class RouteData
{
  private final long itinerary;
  private final String name;
  private final String xml;
  private final Waypoints points;
  
  public RouteData(final long itinerary,
                   final String xml,
                   final Waypoints points,
                   final String name) {
    this.itinerary = itinerary;
    this.xml = xml;
    this.points = points;
    this.name = name;
  }

  public long itinerary() { return itinerary; }
  public String name() { return name; }
  public String xml() { return xml; }
  public Waypoints points() { return points; }
}
