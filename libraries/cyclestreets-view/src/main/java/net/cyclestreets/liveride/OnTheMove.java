package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.Journey;

final class OnTheMove extends MovingState
{
  OnTheMove(final LiveRideState previous) 
  {
    super(previous, CycleStreetsPreferences.nearingTurnDistance());
  } // OnTheMove
  
  @Override
  protected LiveRideState transitionState(final Journey journey)
  {
    return new NearingTurn(this, journey);
  } // transitionStatue
} // class OnTheMove
  