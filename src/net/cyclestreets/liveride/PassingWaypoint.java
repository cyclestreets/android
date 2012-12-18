package net.cyclestreets.liveride;

import net.cyclestreets.api.Journey;

import org.osmdroid.util.GeoPoint;

final class PassingWaypoint extends LiveRideState
{
  PassingWaypoint(final LiveRideState previous) 
  {
    super(previous);
    notify("Passing waypoint");
  } // PassingWaypoint

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam)
  {
    int current = journey.activeSegmentIndex();
    return new AdvanceToSegment(this, journey, journey.segments().get(++current));
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
} // class PassingWaypoint
