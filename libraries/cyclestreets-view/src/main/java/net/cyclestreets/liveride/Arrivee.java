package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

final class Arrivee extends LiveRideState
{
  public static final String ARRIVERAI = "Arreeve eh";

  Arrivee(final LiveRideState previous) {
    super(previous);
    notify(ARRIVERAI, "Arriv\u00e9e");
  }

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy) {
    return new Stopped(context());
  }

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
}
