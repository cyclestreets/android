package net.cyclestreets.routing;

import java.util.*;

import android.support.annotation.NonNull;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class Waypoints implements Iterable<IGeoPoint>
{
  public static final Waypoints NULL_WAYPOINTS = new Waypoints();

  public static Waypoints fromTo(final IGeoPoint start, final IGeoPoint end) {
    return new Waypoints(start, end);
  }

  private final LinkedList<IGeoPoint> waypoints = new LinkedList<>();

  public Waypoints() {}

  private Waypoints(final IGeoPoint from, final IGeoPoint to) {
    waypoints.add(from);
    waypoints.add(to);
  }

  public Waypoints(final List<IGeoPoint> points) {
    waypoints.addAll(points);
  }

  public int count() { return waypoints.size(); }
  public boolean isEmpty() { return waypoints.isEmpty(); }

  public IGeoPoint first() { return waypoints.isEmpty() ? null : waypoints.getFirst(); }
  public IGeoPoint last() { return waypoints.getLast(); }

  public void add(final double lat, final double lon) { add(new GeoPoint(lat, lon)); }
  public void add(final IGeoPoint geopoint) { waypoints.add(geopoint); }

  public IGeoPoint get(final int i) { return waypoints.get(i); }

  @NonNull
  @Override
  public Iterator<IGeoPoint> iterator() {
    return waypoints.iterator();
  }

  public Waypoints reversed() {
    final List<IGeoPoint> points = new ArrayList<>(waypoints);
    Collections.reverse(points);
    return new Waypoints(points);
  }
}
