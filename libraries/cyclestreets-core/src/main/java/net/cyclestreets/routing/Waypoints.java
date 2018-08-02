package net.cyclestreets.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.support.annotation.NonNull;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class Waypoints implements Iterable<IGeoPoint>
{
  public static final Waypoints NULL_WAYPOINTS = new Waypoints();

  public static Waypoints fromTo(final IGeoPoint start, final IGeoPoint end) {
    return new Waypoints(start, end);
  }

  private final List<IGeoPoint> waypoints;

  public Waypoints() {
    waypoints = new ArrayList<>();
  }

  private Waypoints(final IGeoPoint from, final IGeoPoint to) {
    waypoints = new ArrayList<>();
    waypoints.add(from);
    waypoints.add(to);
  }

  private Waypoints(final List<IGeoPoint> points) {
    waypoints = points;
  }

  public int count() { return waypoints.size(); }
  public boolean isEmpty() { return count() == 0; }

  public IGeoPoint first() { return count() != 0 ? waypoints.get(0) : null; }
  public IGeoPoint last() { return waypoints.get(count()-1); }

  public void add(final double lat, final double lon) { add(new GeoPoint(lat, lon)); }
  public void add(final IGeoPoint geopoint) { waypoints.add(geopoint); }
  public void addAll(final List<IGeoPoint> geopoints) { waypoints.addAll(geopoints); }

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
