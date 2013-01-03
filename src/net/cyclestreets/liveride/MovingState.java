package net.cyclestreets.liveride;

import net.cyclestreets.api.Journey;

import org.osmdroid.util.GeoPoint;

abstract class MovingState extends LiveRideState
{
  MovingState(final LiveRideState previous) 
  {
    super(previous);
  } // OnTheMove
  
  protected LiveRideState checkCourse(Journey journey, GeoPoint whereIam)
  {
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
