package net.cyclestreets.routing;

import java.util.LinkedList;
import java.util.List;

public class ElevationProfile {
  private final LinkedList<Elevation> profile = new LinkedList<>();
  private int min = Integer.MAX_VALUE;
  private int max = Integer.MIN_VALUE;

  void add(List<Elevation> segmentProfile) {
    int distanceUpToSegmentStart = profile.size() != 0 ? profile.getLast().distance() : 0;

    for (Elevation e : segmentProfile) {
      if ((e.distance() == 0) && (distanceUpToSegmentStart != 0))
        continue;

      addProfileEntry(new Elevation(distanceUpToSegmentStart + e.distance(), e.elevation()));
    }
  }

  private void addProfileEntry(Elevation e) {
    min = Math.min(min, e.elevation());
    max = Math.max(max, e.elevation());

    profile.add(e);
  }

  public Iterable<Elevation> profile() {
    return profile;
  }

  public int minimum() { return min; }
  public int maximum() { return max; }
}
