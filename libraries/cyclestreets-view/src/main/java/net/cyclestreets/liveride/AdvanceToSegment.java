package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;

import org.osmdroid.util.GeoPoint;

final class AdvanceToSegment extends LiveRideState
{
  AdvanceToSegment(final LiveRideState previous,
                   final Journey journey)
  {
    this(previous, journey, journey.segments().get(journey.activeSegmentIndex()+1));
  } // AdvanceToSegment    
  
  AdvanceToSegment(final LiveRideState previous,
                   final Journey journey,
                   final Segment segment) 
  {
    super(previous);
    journey.setActiveSegment(segment);
    notify(segment);
  } // AdvanceToSegment
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    if(journey.atWaypoint())
      return new PassingWaypoint(this);
    if(journey.atEnd())
      return new Arrivee(this);
    
    return new OnTheMove(this);
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
}
