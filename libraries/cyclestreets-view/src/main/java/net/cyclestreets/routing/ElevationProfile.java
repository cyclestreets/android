package net.cyclestreets.routing;

import java.util.LinkedList;
import java.util.List;

public class ElevationProfile {
  private final LinkedList<Elevation> profile = new LinkedList<>();
  private int min = Integer.MAX_VALUE;
  private int max = Integer.MIN_VALUE;
  private int totalElevationGain = 0;
  private int totalElevationLoss = 0;

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

    if (!profile.isEmpty()) {
      Elevation last = profile.getLast();
      int elevationGain = e.elevation() - last.elevation();
      if (elevationGain > 0) {
        totalElevationGain += elevationGain;
      } else {
        totalElevationLoss -= elevationGain;
      }
    }

    profile.add(e);
  }

  public Iterable<Elevation> profile() {
    return profile;
  }

  public int totalElevationGain() {
    return totalElevationGain;
  }

  public int totalElevationLoss() {
    return totalElevationLoss;
  }

  public int minimum() { return min; }
  public int maximum() { return max; }
}
