package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

final class Arrivee extends LiveRideState
{
  Arrivee(final LiveRideState previous) 
  {
    super(previous);
    notify("Arreeve eh", "Arriv\u00e9e");
  } // Arrivee
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    return new Stopped(context());
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
}
