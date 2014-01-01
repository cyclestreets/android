package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;

import org.osmdroid.util.GeoPoint;

final class HuntForSegment extends LiveRideState
{
  private int waitToSettle_;
  
  HuntForSegment(final LiveRideState state) 
  {
    super(state);
    waitToSettle_ = 5;
  } // HuntForSegment
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    if(waitToSettle_ > 0) {
      --waitToSettle_;
      return this;
    }
    
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

    if(distance > CycleStreetsPreferences.replanDistance())
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
