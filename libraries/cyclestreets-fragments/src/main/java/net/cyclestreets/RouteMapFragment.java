package net.cyclestreets;

import net.cyclestreets.content.RouteSummary;
import net.cyclestreets.fragments.R;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.GPS;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.views.overlay.POIOverlay;
import net.cyclestreets.views.overlay.RouteOverlay;
import net.cyclestreets.views.overlay.RouteHighlightOverlay;
import net.cyclestreets.views.overlay.TapToRouteOverlay;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.enableMenuItem;
import static net.cyclestreets.util.MenuHelper.showMenuItem;

public class RouteMapFragment extends CycleMapFragment
                              implements Route.Listener
{
	private TapToRouteOverlay routeSetter_;
	private POIOverlay poiOverlay_;
	private boolean hasGps_;

	@Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved)
  {
    final View v = super.onCreateView(inflater, container, saved);

	  overlayPushBottom(new RouteHighlightOverlay(getActivity(), mapView()));

    poiOverlay_ = new POIOverlay(getActivity(), mapView());
    overlayPushBottom(poiOverlay_);

	  overlayPushBottom(new RouteOverlay(getActivity()));

	  routeSetter_ = new TapToRouteOverlay(getActivity(), mapView());
	  overlayPushTop(routeSetter_);

	  hasGps_ = GPS.deviceHasGPS(getActivity());

	  return v;
  } // onCreate

	@Override
	public void onResume()
	{
	  super.onResume();
	  Route.registerListener(this);
	  Route.onResume();
  } // onResume

  @Override
  public void onPause()
  {
    Route.setWaypoints(routeSetter_.waypoints());
    Route.unregisterListener(this);
    super.onPause();
  } // onPause

	public void onRouteNow(int itinerary)
	{
	  Route.FetchRoute(CycleStreetsPreferences.routeType(),
	                   itinerary,
	                   CycleStreetsPreferences.speed(),
	                   getActivity());
	} // onRouteNow

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
    inflater.inflate(R.menu.route_map, menu);
    super.onCreateOptionsMenu(menu, inflater);
	} // onCreateOptionsMenu

	@Override
	public void onPrepareOptionsMenu(final Menu menu)
	{
    showMenuItem(menu, R.id.ic_menu_liveride, Route.available() && hasGps_);
	  enableMenuItem(menu, R.id.ic_menu_directions, true);
	  showMenuItem(menu, R.id.ic_menu_saved_routes, Route.storedCount() != 0);
	  enableMenuItem(menu, R.id.ic_menu_route_number, true);
		super.onPrepareOptionsMenu(menu);
	} // onPrepareOptionsMenu

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if(super.onOptionsItemSelected(item))
			return true;

    final int menuId = item.getItemId();
    if(R.id.ic_menu_liveride == menuId) {
      startLiveRide();
      return true;
    }
    if(R.id.ic_menu_directions == menuId) {
      launchRouteDialog();
      return true;
    }
    if(R.id.ic_menu_saved_routes == menuId) {
      launchStoredRoutesDialog();
      return true;
    }
    if(R.id.ic_menu_route_number == menuId) {
      launchFetchRouteDialog();
      return true;
		}

		return false;
	} // onMenuItemSelected

	@Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
	  super.onActivityResult(requestCode, resultCode, data);

		if(resultCode != Activity.RESULT_OK)
			return;

		if(requestCode == ActivityId.Directions)
		{
		  final Waypoints points = GeoIntent.getWaypoints(data);
			final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
			final int speed = data.getIntExtra(CycleStreetsConstants.EXTRA_ROUTE_SPEED,
					                               CycleStreetsPreferences.speed());
			Route.PlotRoute(routeType,
			                speed,
			                getActivity(),
                      points);
		} // if ...

		if(requestCode == ActivityId.RouteNumber)
		{
		  final long routeNumber = data.getLongExtra(CycleStreetsConstants.EXTRA_ROUTE_NUMBER, -1);
		  final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
      final int speed = data.getIntExtra(CycleStreetsConstants.EXTRA_ROUTE_SPEED,
                                         CycleStreetsPreferences.speed());

      Route.FetchRoute(routeType, routeNumber, speed, getActivity());
		} // if ...
	} // onActivityResult

	private void startLiveRide()
	{
	  LiveRideActivity.launch(getActivity());
	} // startLiveRide

  private void launchRouteDialog()
  {
    startNewRoute(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                      doLaunchRouteDialog();
                    }
                  });
  } // launchRouteDialog

	private void doLaunchRouteDialog()
	{
	  final Intent intent = new Intent(getActivity(), RouteByAddressActivity.class);
	  GeoIntent.setBoundingBox(intent, mapView().getBoundingBox());
	  final Location lastFix = mapView().getLastFix();
	  GeoIntent.setLocation(intent, lastFix);
      GeoIntent.setWaypoints(intent, routeSetter_.waypoints());
	  startActivityForResult(intent, ActivityId.Directions);
	} // doLaunchRouteDialog

	private void launchFetchRouteDialog()
	{
	  startNewRoute(new DialogInterface.OnClickListener() {
	                  public void onClick(DialogInterface arg0, int arg1) {
	                    doLaunchFetchRouteDialog();
	                  }
                  });
	} // launchFetchRouteDialog

	private void doLaunchFetchRouteDialog()
	{
	  final Intent intent = new Intent(getActivity(), RouteNumberActivity.class);
	  startActivityForResult(intent, ActivityId.RouteNumber);
	} // doLaunchFetchRouteDialog

	private void launchStoredRoutesDialog()
	{
    RouteSummaryAdapter rsa = new RouteSummaryAdapter(getActivity());
    AlertDialog ad = Dialog.listViewDialog(getActivity(),
                                           R.string.ic_menu_saved_routes,
                                           rsa,
                                           null,
                                           null);
    rsa.setDialog(ad);
	} // launchStoredRoutesDialog

	private void startNewRoute(final DialogInterface.OnClickListener listener)
	{
    if(Route.available() && CycleStreetsPreferences.confirmNewRoute())
      MessageBox.YesNo(mapView(),
                       R.string.confirm_new_route,
                       listener);
    else
      listener.onClick(null, 0);
	} // startNewRoute

	@Override
	public void onNewJourney(final Journey journey, final Waypoints waypoints)
	{
	  if(!waypoints.isEmpty())
	    mapView().getController().setCenter(waypoints.first());
	  mapView().postInvalidate();
	} // onNewJourney

	@Override
	public void onResetJourney()
	{
    mapView().invalidate();
	} // onReset

  //////////////////////////////////////////////
  //////////////////////////////////////////////
  //////////////////////////////////
  private static class RouteSummaryAdapter extends BaseAdapter
                                   implements View.OnClickListener,
                                              View.OnLongClickListener,
                                              View.OnCreateContextMenuListener {
    private final Context context_;
    private final LayoutInflater inflater_;
    private List<RouteSummary> routes_;
    private final Map<View, Integer> viewRoute_;
    private AlertDialog ad_;

    RouteSummaryAdapter(final Context context) {
      context_ = context;
      inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      routes_ = Route.storedRoutes();
      viewRoute_ = new HashMap<>();
    } // SegmentAdaptor

    public void setDialog(final AlertDialog ad) { ad_ = ad; }

    private void refresh() {
      routes_ = Route.storedRoutes();
      notifyDataSetChanged();
      if (routes_.size() == 0)
        closeDialog();
    } // refresh

    private void closeDialog() {
      if (ad_ != null)
        ad_.cancel();
    } // closeDialog

    public RouteSummary getRouteSummary(int localId) {
      for(final RouteSummary r : routes_)
        if(r.localId() == localId)
          return r;
      return null;
    } // getRouteSummary

    @Override
    public int getCount() { return routes_.size(); }

    @Override
    public Object getItem(int position) { return routes_.get(position); }

    @Override
    public long getItemId(int position) { return routes_.get(position).localId(); }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      final RouteSummary summary = routes_.get(position);
      final View v = inflater_.inflate(R.layout.storedroutes_item, parent, false);
      viewRoute_.put(v, summary.localId());

      final TextView n = (TextView)v.findViewById(R.id.route_title);

      final String p = summary.plan();
      final String plan = p.substring(0,1).toUpperCase() + p.substring(1);

      n.setText(summary.title() + "\n" +
          plan + " route, " +
          Segment.formatter.total_distance(summary.distance()));

      v.setOnClickListener(this);
      v.setOnLongClickListener(this);
      v.setOnCreateContextMenuListener(this);

      return v;
    } // getView

    @Override
    public void onClick(final View view) {
      final int localId = viewRoute_.get(view);
      openRoute(localId);
    } // onClick

    @Override
    public boolean onLongClick(final View view) {
      view.showContextMenu();
      return true;
    } // onClick

    @Override
    public void onCreateContextMenu(final ContextMenu menu,
                                    final View view,
                                    final ContextMenu.ContextMenuInfo contextMenuInfo) {
      final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(final MenuItem item) {
          RouteSummaryAdapter.this.onViewMenuClick(view, item);
          return true;
        } // onMenuItemClick
      };
      createMenuItem(menu, R.string.ic_menu_open).setOnMenuItemClickListener(listener);
      createMenuItem(menu, R.string.ic_menu_rename).setOnMenuItemClickListener(listener);
      createMenuItem(menu, R.string.ic_menu_delete).setOnMenuItemClickListener(listener);
    } // onCreateContextMenu

    private void onViewMenuClick(final View view, final MenuItem item) {
      final int localId = viewRoute_.get(view);
      final int menuId = item.getItemId();

      if(R.string.ic_menu_open == menuId)
        openRoute(localId);
      if(R.string.ic_menu_rename == menuId)
        renameRoute(localId);
      if(R.string.ic_menu_delete == menuId)
        deleteRoute(localId);
    } // onMenuItemClick

    /////////////////////////////////////////////
    private void openRoute(final int localId) {
      Route.PlotStoredRoute(localId, context_);
      closeDialog();
    } // routeSelected

    private void renameRoute(final int localId)
    {
      final RouteSummary route = getRouteSummary(localId);
      Dialog.editTextDialog(context_, route.title(), "Rename",
          new Dialog.UpdatedTextListener() {
            @Override
            public void updatedText(final String updated) {
              Route.RenameRoute(localId, updated);
              refresh();
            } // updatedText
          });
    } // renameRoute

    private void deleteRoute(final int localId) {
      Route.DeleteRoute(localId);
      refresh();
    } // deleteRoute
  } // class RouteSummaryAdaptor
} // class MapActivity
