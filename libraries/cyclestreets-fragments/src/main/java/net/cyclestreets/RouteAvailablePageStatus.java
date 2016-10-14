package net.cyclestreets;

import android.widget.BaseAdapter;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;

public class RouteAvailablePageStatus
    implements MainNavDrawerActivity.PageStatus,
               Route.Listener {
  private BaseAdapter adapter_;

  public RouteAvailablePageStatus() {
    Route.registerListener(this);
  } // RouteAvailablePageStatus

  @Override
  public void setAdapter(final BaseAdapter adapter) {
    adapter_ = adapter;
  } // setAdapter

  @Override
  public boolean enabled() {
    return Route.available();
  } // enabled

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    ping();
  } // onNewJourney

  public void onResetJourney() {
    ping();
  } // onResetJourney

  private void ping() {
    if (adapter_ != null)
      adapter_.notifyDataSetChanged();
  } // ping

} // RouteAvailablePageStatus
