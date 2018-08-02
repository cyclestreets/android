package net.cyclestreets.routing.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.routing.Elevation;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public final class SegmentDomainObject {

  public final String name;
  public final String turn;
  public final int distance;
  public final int time;
  public final boolean shouldWalk;
  public final int legNumber;
  public final List<IGeoPoint> points = new ArrayList<>();
  public final List<Elevation> segmentProfile = new ArrayList<>();

  @JsonCreator
  SegmentDomainObject(@JsonProperty("name") String name,
                      @JsonProperty("turn") String turn,
                      @JsonProperty("distance") int distance,
                      @JsonProperty("time") int time,
                      @JsonProperty("walk") String walk,
                      @JsonProperty("legNumber") int legNumber,
                      @JsonProperty("distances") String distances,
                      @JsonProperty("elevations") String elevations,
                      @JsonProperty("points") String points) {
    this.name = name;
    this.turn = turn;
    this.distance = distance;
    this.time = time;
    this.shouldWalk = "1".equals(walk);
    this.legNumber = legNumber;
    constructProfile(distances, elevations);
    unpackPoints(points);
  }

  private void unpackPoints(String packedPoints) {
    final String[] coords = packedPoints.split(" ");
    for (final String coord : coords) {
      final String[] yx = coord.split(",");
      final GeoPoint p = new GeoPoint(Double.parseDouble(yx[1]), Double.parseDouble(yx[0]));
      points.add(p);
    }
  }

  private void constructProfile(String distances, String elevations) {
    final String[] dists = distances.split(",");
    final String[] els = elevations.split(",");
    int cumulativeDistance = 0;
    for (int i = 0; i != dists.length; ++i) {
      int distance = Integer.parseInt(dists[i]);
      int elevation = Integer.parseInt(els[i]);

      cumulativeDistance += distance;
      segmentProfile.add(new Elevation(cumulativeDistance, elevation));
    }
  }

  @Override
  public String toString() {
    return "SegmentDomainObject{" + "name='" + name + '\'' +
        ", turn='" + turn + '\'' +
        ", distance=" + distance +
        ", time=" + time +
        ", shouldWalk=" + shouldWalk +
        ", legNumber=" + legNumber +
        ", points=" + points +
        ", segmentProfile=" + segmentProfile +
        '}';
  }
}
