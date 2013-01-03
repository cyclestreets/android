package net.cyclestreets.liveride;

import net.cyclestreets.api.Journey;

import org.osmdroid.util.GeoPoint;

final class OnTheMove extends MovingState
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

    return checkCourse(journey, whereIam);
  } // update
} // class OnTheMove
