package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static net.cyclestreets.api.client.dto.Waypoint.WaypointWrapper;
import static net.cyclestreets.api.client.dto.RouteOrSegment.RouteOrSegmentWrapper;

public final class JourneyDto {

  private Route route;

  private final List<Segment> segments = new ArrayList<>();
  private final List<Waypoint> waypoints = new ArrayList<>();

  @JsonCreator
  public JourneyDto(@JsonProperty(value = "waypoint") List<WaypointWrapper> waypointWrappers,
                    @JsonProperty(value = "marker") List<RouteOrSegmentWrapper> routeOrSegmentWrappers) {
    for (WaypointWrapper waypointWrapper : waypointWrappers) {
      waypoints.add(waypointWrapper.getContents());
    }
    for (RouteOrSegmentWrapper ros : routeOrSegmentWrappers) {
      System.out.println(ros.getContents());
      if (Route.class.isInstance(ros.getContents())) {
        route = (Route)ros.getContents();
      } else {
        segments.add((Segment)ros.getContents());
      }
    }
  }

  public Route getRoute() {
    return route;
  }

  public List<Segment> getSegments() {
    return segments;
  }

  public List<Waypoint> getWaypoints() {
    return waypoints;
  }
}
