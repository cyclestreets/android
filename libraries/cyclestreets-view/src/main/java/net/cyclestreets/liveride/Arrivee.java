package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

final class Arrivee extends LiveRideState
{
  public static final String ARRIVEE = "Arrivée";

  Arrivee(final LiveRideState previous) {
    super(previous);
    notify(ARRIVEE, ARRIVEE);
  }

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy) {
    return new Stopped(getContext());
  }

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
}
