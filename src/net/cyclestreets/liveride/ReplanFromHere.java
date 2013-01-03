package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Waypoints;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

final class ReplanFromHere extends LiveRideState
      implements Route.Listener
{
  private LiveRideState next_;

  ReplanFromHere(final LiveRideState previous, final GeoPoint whereIam)
  {
    super(previous);
    notify("Too far away. Re-planning the journey.");

    next_ = this;

    final GeoPoint finish = Route.waypoints().last();    
    Route.registerListener(this);
    Route.PlotRoute(CycleStreetsPreferences.routeType(), 
                    CycleStreetsPreferences.speed(),
                    context(),
                    Waypoints.fromTo(whereIam, finish));
  } // ReplanFromHere
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam)
  {
    return next_;
  } // update

  @Override
  public boolean isStopped() { return false; }

  @Override
  public boolean arePedalling() { return true; }

  @Override
  public void onNewJourney(Journey journey, Waypoints waypoints)
  {
    next_ = new HuntForSegment(this);
    Route.unregisterListener(this);
  } // onNewJourney

  @Override
  public void onResetJourney()
  {
  } // onResetJourney
} // class ReplanFromHere
