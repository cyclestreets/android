package net.cyclestreets.liveride;

import net.cyclestreets.api.Journey;

import org.osmdroid.util.GeoPoint;

final class NearingTurn extends LiveRideState
{
  NearingTurn(final LiveRideState previous) 
  {
    super(previous);
    notify("Get ready");
  } // NearingEnd

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam)
  {
    final int distanceFromEnd = journey.activeSegment().distanceFromEnd(whereIam);
    if(distanceFromEnd > IMMEDIATE_DISTANCE)
      return new AdvanceToSegment(this, journey);
    
    // off course?   
    return this;
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
} // class NearingTurn
