package net.cyclestreets.routing;

import java.util.ArrayList;
import java.util.List;

public class ElevationProfile {
  private List<Elevation> profile_;

  ElevationProfile() {
    profile_ = new ArrayList<>();
  } // ElevationProfile

  void add(List<Elevation> segmentProfile) {
    int cumulativeDistance = profile_.size() != 0 ? profile_.get(profile_.size() - 1).distance() : 0;

    for (Elevation e : segmentProfile) {
      if ((e.distance() == 0) && (cumulativeDistance != 0))
        continue;
      profile_.add(new Elevation(e.distance() + cumulativeDistance, e.elevation()));
    } // append
  } // add

  public Iterable<Elevation> profile() {
    return profile_;
  } // profile
} // Elevationprofile
