package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

abstract class MovingState extends LiveRideState
{
  private final int transition_;
  
  MovingState(final LiveRideState previous, final int transitionThreshold) 
  {
    super(previous);
    transition_ = transitionThreshold;
  } // OnTheMove
  
  @Override
  public final LiveRideState update(final Journey journey, final GeoPoint whereIam, final int accuracy)
  {
    int distanceFromEnd = journey.activeSegment().distanceFromEnd(whereIam);
    distanceFromEnd -= accuracy;
    if(distanceFromEnd < transition_)
      return transitionState(journey);

    return checkCourse(journey, whereIam, accuracy);
  } // update
  
  protected abstract LiveRideState transitionState(final Journey journey);
    
  private LiveRideState checkCourse(final Journey journey, final GeoPoint whereIam, final int accuracy)
  {
    int distance = journey.activeSegment().distanceFrom(whereIam);
    distance -= accuracy;
    
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
