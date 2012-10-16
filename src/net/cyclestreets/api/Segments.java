package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public class Segments implements Iterable<Segment>
{
  private final List<Segment> segments_;
  
  public Segments() 
  {
    segments_ = new ArrayList<Segment>();
  } // Segments
  
  public int count() { return segments_.size(); }
  public boolean isEmpty() { return count() == 0; }
  
  public GeoPoint startPoint() { return segments_.get(0).start(); }
  public GeoPoint finishPoint() { return segments_.get(count()-1).finish(); }
  public Segment.Start first() { return (Segment.Start)segments_.get(0); }
  public Segment.End last() { return (Segment.End)segments_.get(count()-1); }
  
  public void add(final Segment seg) 
  { 
    if(seg instanceof Segment.Start)
      segments_.add(0, seg);
    else
      segments_.add(seg); 
  } // add
  
  public Segment get(final int i) { return segments_.get(i); }
  
  @Override
  public Iterator<Segment> iterator() { return segments_.iterator(); }
  
  public Iterator<GeoPoint> pointsIterator() { return new PointsIterator(this); }
  
  static private class PointsIterator implements Iterator<GeoPoint>
  {
    private final Iterator<Segment> segments_;
    private Iterator<GeoPoint> points_;
    
    PointsIterator(final Segments segments)
    {
      segments_ = segments.iterator();
      if(!segments_.hasNext())
        return;
      
      points_ = segments_.next().points();
    } // PointsIterator
    
    @Override
    public boolean hasNext() 
    {
      return points_ != null && points_.hasNext();
    } // hasNext

    @Override
    public GeoPoint next() 
    {
      if(!hasNext())
        throw new IllegalStateException();
      
      final GeoPoint p = points_.next();
      
      if(!hasNext())
      {
        if(segments_.hasNext())
          points_ = segments_.next().points();
        else
          points_ = null;
      } // if ...
      
      return p;
    } // next

    @Override
    public void remove() 
    {
      throw new UnsupportedOperationException();
    } // remove
  } // class PointsIterator

} // class Segments
