package net.cyclestreets.routing;

public class Elevation {
  private final int distanceFromStart;
  private final int elevation;

  public Elevation(final int d, final int e) {
    distanceFromStart = d;
    elevation = e;
  }

  public int distance() { return distanceFromStart; }
  public int elevation() { return elevation; }
}
