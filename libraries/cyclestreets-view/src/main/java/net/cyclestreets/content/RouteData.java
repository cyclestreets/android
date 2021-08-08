package net.cyclestreets.content;

import net.cyclestreets.routing.Waypoints;

public class RouteData
{
  private final String name;
  private final String json;
  private final Waypoints points;
  private final boolean saveRoute;

  public RouteData(final String json,
                   final Waypoints points,
                   final String name,
                   final boolean saveRoute) {
    this.json = json;
    this.points = points;
    this.name = name;
    this.saveRoute = saveRoute;
  }

  public String name() { return name; }
  public String json() { return json; }
  public Waypoints points() { return points; }
  public boolean saveRoute() { return saveRoute; }
}
