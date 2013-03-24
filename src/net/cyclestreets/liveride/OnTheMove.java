package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

final class OnTheMove extends MovingState
{
  OnTheMove(final LiveRideState previous) 
  {
    super(previous, NEAR_DISTANCE);
  } // OnTheMove
  
  @Override
  protected LiveRideState transitionState(final Journey journey)
  {
    return new NearingTurn(this);
  } // transitionStatue
} // class OnTheMove
