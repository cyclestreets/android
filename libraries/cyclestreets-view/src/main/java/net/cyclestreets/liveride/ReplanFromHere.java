package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

final class ReplanFromHere extends LiveRideState implements Route.Listener
{
  private LiveRideState next_;

  ReplanFromHere(final LiveRideState previous, final GeoPoint whereIam) {
    super(previous);
    notify("Too far away. Re-planning the journey.");

    next_ = this;

    final IGeoPoint finish = Route.waypoints().last();
    Route.softRegisterListener(this);
    Route.PlotRoute(CycleStreetsPreferences.routeType(),
                    CycleStreetsPreferences.speed(),
                    getContext(),
                    Waypoints.fromTo(whereIam, finish));
  }

  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy) {
    return next_;
  }

  @Override
  public boolean isStopped() { return false; }

  @Override
  public boolean arePedalling() { return true; }

  @Override
  public void onNewJourney(Journey journey, Waypoints waypoints) {
    next_ = new HuntForSegment(this);
    Route.unregisterListener(this);
  }

  @Override
  public void onResetJourney() {
  }
}
