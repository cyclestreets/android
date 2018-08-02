package net.cyclestreets.routing.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.osmdroid.api.IGeoPoint;

import java.util.ArrayList;
import java.util.List;

public class JourneyDomainObject {
  @JsonProperty
  public final List<IGeoPoint> waypoints = new ArrayList<>();
  @JsonProperty
  public final RouteDomainObject route = new RouteDomainObject();
  @JsonProperty
  public final List<SegmentDomainObject> segments = new ArrayList<>();

  @Override
  public String toString() {
    return "JourneyDomainObject{" + "waypoints=" + waypoints +
        ", route=" + route +
        ", segments=" + segments +
        '}';
  }
}
