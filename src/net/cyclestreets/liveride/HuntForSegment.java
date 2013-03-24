package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;

import org.osmdroid.util.GeoPoint;

final class HuntForSegment extends LiveRideState
{
  HuntForSegment(final LiveRideState state) 
  {
    super(state);
  } // HuntForSegment
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    Segment nearestSeg = null;
    int distance = Integer.MAX_VALUE;
    
    for(final Segment seg : journey.segments())
    {
      int from = seg.distanceFrom(whereIam);
      if(from < distance) 
      {
        distance = from;
        nearestSeg = seg;
      } // if ...
    } // for ...
    
    distance -= accuracy;

    if(distance > FAR_DISTANCE)
      return new ReplanFromHere(this, whereIam);

    if(nearestSeg == journey.activeSegment())
      return new OnTheMove(this);
    
    return new AdvanceToSegment(this, journey, nearestSeg);
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
}
