package net.cyclestreets.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public class Waypoints implements Iterable<GeoPoint>
{
  public static final Waypoints NULL_WAYPOINTS = new Waypoints();
  
  public static Waypoints fromTo(final GeoPoint start, final GeoPoint end) 
  {
    return new Waypoints(start, end);
  } // fromTo
  
  private final List<GeoPoint> waypoints_;
  
  public Waypoints() 
  {
    waypoints_ = new ArrayList<GeoPoint>();
  } // Waypoints
  
  private Waypoints(final GeoPoint from, final GeoPoint to)
  {
    waypoints_ = new ArrayList<GeoPoint>();
    waypoints_.add(from);
    waypoints_.add(to);
  } // Waypoints
  
  private Waypoints(final List<GeoPoint> points)
  {
    waypoints_ = points;
  } // Waypoints

  public int count() { return waypoints_.size(); }
  public boolean isEmpty() { return count() == 0; }
  
  public GeoPoint first() { return count() != 0 ? waypoints_.get(0) : null; }
  public GeoPoint last() { return waypoints_.get(count()-1); }
  
  public void add(final double lat, final double lon) { add(new GeoPoint(lat, lon)); }
  public void add(final GeoPoint geopoint) { waypoints_.add(geopoint); }
  
  public GeoPoint get(final int i) { return waypoints_.get(i); }
  
  @Override
  public Iterator<GeoPoint> iterator()
  {
    return waypoints_.iterator();
  } // Waypoints
  
  public Waypoints reversed() 
  {
    final List<GeoPoint> points = new ArrayList<GeoPoint>(waypoints_);
    Collections.reverse(points);
    return new Waypoints(points);
  } // reversed
} // class Waypoints
