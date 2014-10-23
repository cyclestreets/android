package net.cyclestreets.routing;

import java.util.ArrayList;
import java.util.List;

public class ElevationProfile {
  private List<Elevation> profile_;
  private int min_;
  private int max_;

  ElevationProfile() {
    profile_ = new ArrayList<>();
    min_ = Integer.MAX_VALUE;
    max_ = Integer.MIN_VALUE;
  } // ElevationProfile

  void add(List<Elevation> segmentProfile) {
    int cumulativeDistance = profile_.size() != 0 ? profile_.get(profile_.size() - 1).distance() : 0;

    for (Elevation e : segmentProfile) {
      if ((e.distance() == 0) && (cumulativeDistance != 0))
        continue;
      profile_.add(new Elevation(e.distance() + cumulativeDistance, e.elevation()));

      min_ = Math.min(min_, e.elevation());
      max_ = Math.max(max_, e.elevation());
    } // append
  } // add

  public Iterable<Elevation> profile() {
    return profile_;
  } // profile

  public int minimum() { return min_; }
  public int maximum() { return max_; }
} // Elevationprofile
