package net.cyclestreets.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class Waypoints implements Iterable<IGeoPoint>
{
  public static final Waypoints NULL_WAYPOINTS = new Waypoints();
  
  public static Waypoints fromTo(final IGeoPoint start, final IGeoPoint end)
  {
    return new Waypoints(start, end);
  } // fromTo
  
  private final List<IGeoPoint> waypoints_;
  
  public Waypoints() 
  {
    waypoints_ = new ArrayList<>();
  } // Waypoints
  
  private Waypoints(final IGeoPoint from, final IGeoPoint to)
  {
    waypoints_ = new ArrayList<>();
    waypoints_.add(from);
    waypoints_.add(to);
  } // Waypoints
  
  private Waypoints(final List<IGeoPoint> points)
  {
    waypoints_ = points;
  } // Waypoints

  public int count() { return waypoints_.size(); }
  public boolean isEmpty() { return count() == 0; }
  
  public IGeoPoint first() { return count() != 0 ? waypoints_.get(0) : null; }
  public IGeoPoint last() { return waypoints_.get(count()-1); }
  
  public void add(final double lat, final double lon) { add(new GeoPoint(lat, lon)); }
  public void add(final IGeoPoint geopoint) { waypoints_.add(geopoint); }
  
  public IGeoPoint get(final int i) { return waypoints_.get(i); }
  
  @Override
  public Iterator<IGeoPoint> iterator()
  {
    return waypoints_.iterator();
  } // Waypoints
  
  public Waypoints reversed() 
  {
    final List<IGeoPoint> points = new ArrayList<>(waypoints_);
    Collections.reverse(points);
    return new Waypoints(points);
  } // reversed
} // class Waypoints
