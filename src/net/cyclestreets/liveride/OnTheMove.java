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
    return this;
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
} // class OnTheMove
