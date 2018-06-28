package net.cyclestreets;

import net.cyclestreets.fragments.R;
import net.cyclestreets.util.GPS;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Permissions;
import net.cyclestreets.views.overlay.POIOverlay;
import net.cyclestreets.views.overlay.RouteOverlay;
import net.cyclestreets.views.overlay.RouteHighlightOverlay;
import net.cyclestreets.views.overlay.TapToRouteOverlay;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import static net.cyclestreets.util.MenuHelper.enableMenuItem;
import static net.cyclestreets.util.MenuHelper.showMenuItem;

public class RouteMapFragment extends CycleMapFragment implements Route.Listener
{
  private TapToRouteOverlay routeSetter_;
  private POIOverlay poiOverlay_;
  private boolean hasGps_;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved) {
    Permissions.verify(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
    Permissions.verify(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

    final View v = super.onCreateView(inflater, container, saved);

    overlayPushBottom(new RouteHighlightOverlay(getActivity(), mapView()));

    poiOverlay_ = new POIOverlay(mapView());
    overlayPushBottom(poiOverlay_);

    overlayPushBottom(new RouteOverlay());

    routeSetter_ = new TapToRouteOverlay(mapView());
    overlayPushTop(routeSetter_);

    hasGps_ = GPS.deviceHasGPS(getActivity());

    return v;
  }

  @Override
  public void onResume() {
    super.onResume();
    Route.registerListener(this);
    Route.onResume();
  }

  @Override
  public void onPause() {
    Route.setWaypoints(routeSetter_.waypoints());
    Route.unregisterListener(this);
    super.onPause();
  }

  public void onRouteNow(int itinerary) {
    Route.FetchRoute(CycleStreetsPreferences.routeType(),
        itinerary,
        CycleStreetsPreferences.speed(),
        getActivity());
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    inflater.inflate(R.menu.route_map, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    showMenuItem(menu, R.id.ic_menu_liveride, Route.available() && hasGps_);
    enableMenuItem(menu, R.id.ic_menu_directions, true);
    showMenuItem(menu, R.id.ic_menu_saved_routes, Route.storedCount() != 0);
    enableMenuItem(menu, R.id.ic_menu_route_number, true);
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (super.onOptionsItemSelected(item))
      return true;

    final int menuId = item.getItemId();
    if (R.id.ic_menu_liveride == menuId) {
      startLiveRide();
      return true;
    }
    if (R.id.ic_menu_directions == menuId) {
      launchRouteDialog();
      return true;
    }
    if (R.id.ic_menu_saved_routes == menuId) {
      launchStoredRoutes();
      return true;
    }
    if (R.id.ic_menu_route_number == menuId) {
      launchFetchRouteDialog();
      return true;
    }

    return false;
  }

  private void startLiveRide() {
    LiveRideActivity.launch(getActivity());
  }

  private void launchRouteDialog() {
    startNewRoute(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                      doLaunchRouteDialog();
                    }
                  });
  }

  private void doLaunchRouteDialog() {
    RouteByAddress.launch(getActivity(),
        mapView().getBoundingBox(),
        mapView().getLastFix(),
        routeSetter_.waypoints());
  }

  private void launchFetchRouteDialog() {
    startNewRoute(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                      doLaunchFetchRouteDialog();
                    }
                  });
  }

  private void doLaunchFetchRouteDialog() {
    RouteByNumber.launch(getActivity());
  }

  private void launchStoredRoutes() {
    StoredRoutes.launch(getActivity());
  }

  private void startNewRoute(final DialogInterface.OnClickListener listener) {
    if (Route.available() && CycleStreetsPreferences.confirmNewRoute())
      MessageBox.YesNo(mapView(),
                       R.string.confirm_new_route,
                       listener);
    else
      listener.onClick(null, 0);
  }

  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints) {
    if (!waypoints.isEmpty())
      mapView().getController().setCenter(waypoints.first());
    mapView().postInvalidate();
  }

  @Override
  public void onResetJourney() {
    mapView().invalidate();
  }
}
