package net.cyclestreets.liveride;

import net.cyclestreets.api.Journey;

import org.osmdroid.util.GeoPoint;

final class OnTheMove extends LiveRideState
{
  OnTheMove(final LiveRideState previous) 
  {
    super(previous);
  } // OnTheMove
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam)
  {
    final int distanceFromEnd = journey.activeSegment().distanceFromEnd(whereIam);

    if(distanceFromEnd < NEAR_DISTANCE)
      return new NearingTurn(this);

    final int distance = journey.activeSegment().distanceFrom(whereIam);
    
    if(distance > FAR_DISTANCE)
      return new ReplanFromHere(this, whereIam);

    if(distance > NEAR_DISTANCE)
      return new GoingOffCourse(this);
      
    return this;
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
} // class OnTheMove
