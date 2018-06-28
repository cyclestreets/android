package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

final class PassingWaypoint extends LiveRideState
{
  PassingWaypoint(final LiveRideState previous) {
    super(previous);
    notify("Passing waypoint");
  }

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy) {
    return new AdvanceToSegment(this, journey);
  }

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
}
