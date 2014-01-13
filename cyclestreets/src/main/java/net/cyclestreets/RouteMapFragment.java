package net.cyclestreets;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
	
	public void onStoredRouteNow(final int localId)
	{
	  Route.PlotStoredRoute(localId, getActivity());
  } // onStoredRouteNow
    
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
    createMenuItem(menu, R.string.ic_menu_liveride, Menu.NONE, R.drawable.ic_menu_live_ride);
	  createMenuItem(menu, R.string.ic_menu_directions, Menu.NONE, R.drawable.ic_menu_directions);
	  createMenuItem(menu, R.string.ic_menu_saved_routes, Menu.NONE, R.drawable.ic_menu_places);
	  createMenuItem(menu, R.string.ic_menu_route_number, Menu.NONE, R.drawable.ic_menu_route_number);
    super.onCreateOptionsMenu(menu, inflater);
	} // onCreateOptionsMenu
    
	@Override
	public void onPrepareOptionsMenu(final Menu menu)
	{
    showMenuItem(menu, R.string.ic_menu_liveride, Route.available() && hasGps_);
	  enableMenuItem(menu, R.string.ic_menu_directions, true);
	  showMenuItem(menu, R.string.ic_menu_saved_routes, Route.storedCount() != 0);
	  enableMenuItem(menu, R.string.ic_menu_route_number, true);
		super.onPrepareOptionsMenu(menu);
	} // onPrepareOptionsMenu
    
	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if(super.onOptionsItemSelected(item))
			return true;
		
		switch(item.getItemId())
		{
		  case R.string.ic_menu_liveride:
		    startLiveRide();
		    return true;
		  case R.string.ic_menu_directions:
		    launchRouteDialog();
		    return true;
		  case R.string.ic_menu_saved_routes:
		    launchStoredRoutesDialog();
		    return true;
		  case R.string.ic_menu_route_number:
		    launchFetchRouteDialog();
		    return true;
		} // switch
		
		return false;
	} // onMenuItemSelected
		
	@Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) 
  {
	  super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode != Activity.RESULT_OK)
			return;
		
		if(requestCode == ActivityId.StoredRoutes)
		{
			final int localId = data.getIntExtra(CycleStreetsConstants.ROUTE_ID, 0);
			if(localId != 0)
				onStoredRouteNow(localId);
			return;
		} // if ...
		
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
	  final Intent intent = new Intent(getActivity(), StoredRoutesActivity.class);
    startActivityForResult(intent, ActivityId.StoredRoutes);
	} // launchStoredRoutesDialog
	
	private void startNewRoute(final DialogInterface.OnClickListener listener)
	{
    if(Route.available() && CycleStreetsPreferences.confirmNewRoute())
      MessageBox.YesNo(mapView(),
                       "Start a new route?",
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
} // class MapActivity