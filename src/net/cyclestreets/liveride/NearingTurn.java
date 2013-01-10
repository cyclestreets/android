package net.cyclestreets.liveride;

import net.cyclestreets.api.Journey;

final class NearingTurn extends MovingState
{
  NearingTurn(final LiveRideState previous) 
  {
    super(previous, IMMEDIATE_DISTANCE);
    notify("Get ready");
  } // NearingEnd

  @Override
  protected LiveRideState transitionState(final Journey journey)
  {
    return new AdvanceToSegment(this, journey);
  } // transitionStatue
} // class NearingTurn
