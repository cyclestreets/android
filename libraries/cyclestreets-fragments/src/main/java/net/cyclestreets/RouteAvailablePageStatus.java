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
  }

  @Override
  public void setAdapter(final BaseAdapter adapter) {
    adapter_ = adapter;
  }

  @Override
  public boolean enabled() {
    return Route.available();
  }

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    ping();
  }

  public void onResetJourney() {
    ping();
  }

  private void ping() {
    if (adapter_ != null)
      adapter_.notifyDataSetChanged();
  }

}
