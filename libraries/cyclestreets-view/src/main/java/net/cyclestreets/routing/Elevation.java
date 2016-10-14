package net.cyclestreets.routing;

public class Elevation {
  final int distanceFromStart_;
  final int elevation_;

  Elevation(final int d, final int e) {
    distanceFromStart_ = d;
    elevation_ = e;
  } // Elevation

  public int distance() { return distanceFromStart_; }
  public int elevation() { return elevation_; }
} // class Elevation
