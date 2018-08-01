package net.cyclestreets.routing.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.osmdroid.api.IGeoPoint;

import java.util.ArrayList;
import java.util.List;

public class JourneyDomainObject {
  @JsonProperty
  private final List<IGeoPoint> waypoints = new ArrayList<>();
  @JsonProperty
  private final RouteDomainObject route = null;
  @JsonProperty
  private final List<SegmentDomainObject> segments = new ArrayList<>();

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("JourneyDomainObject{");
    sb.append("waypoints=").append(waypoints);
    sb.append(", route=").append(route);
    sb.append(", segments=").append(segments);
    sb.append('}');
    return sb.toString();
  }
}
