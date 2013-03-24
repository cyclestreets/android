package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

final class PassingWaypoint extends LiveRideState
{
  PassingWaypoint(final LiveRideState previous) 
  {
    super(previous);
    notify("Passing waypoint");
  } // PassingWaypoint

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    return new AdvanceToSegment(this, journey);
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
} // class PassingWaypoint
