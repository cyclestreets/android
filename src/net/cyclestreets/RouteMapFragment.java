package net.cyclestreets;

import java.util.List;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.R;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.views.overlay.POIOverlay;
import net.cyclestreets.views.overlay.RouteOverlay;
import net.cyclestreets.views.overlay.RouteHighlightOverlay;
import net.cyclestreets.views.overlay.TapToRouteOverlay;
import net.cyclestreets.api.Segment;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

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

public class RouteMapFragment extends CycleMapFragment
                              implements TapToRouteOverlay.Callback, 
                                         Route.Callback
{
	private RouteOverlay path_;
	private TapToRouteOverlay routeSetter_;
	private POIOverlay poiOverlay_;
	
	@Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle saved)
  {
    final View v = super.onCreateView(inflater, container, saved);

	  overlayPushBottom(new RouteHighlightOverlay(getActivity(), mapView()));
        
    poiOverlay_ = new POIOverlay(getActivity(), mapView());
    overlayPushBottom(poiOverlay_);

    path_ = new RouteOverlay(getActivity());
	  overlayPushBottom(path_);
	  
	  routeSetter_ = new TapToRouteOverlay(getActivity(), mapView(), this);
	  overlayPushTop(routeSetter_);
	  
	  return v;
  } // onCreate

	@Override
  public void onPause()
	{
	  Route.setWaypoints(routeSetter_.waypoints());
	  super.onPause();
  } // onPause

	@Override
	public void onResume()
	{
	  super.onResume();
	  setJourneyPath(Route.segments(), Route.waypoints());
  } // onResume
     
	public void onRouteNow(final List<GeoPoint> waypoints)
	{
	  Route.PlotRoute(CycleStreetsPreferences.routeType(), 
	                  CycleStreetsPreferences.speed(),
	                  this, 
	                  getActivity(),
	                  waypoints);
	} // onRouteNow

	public void onRouteNow(int itinerary)
	{
	  Route.FetchRoute(CycleStreetsPreferences.routeType(),
	                   itinerary, 
	                   CycleStreetsPreferences.speed(), 
	                   this,
	                   getActivity());
	}
	
	public void reRouteNow(final String plan)
	{
	  Route.RePlotRoute(plan,
	                    this, 
	                    getActivity());
	} // reRouteNow

	public void onStoredRouteNow(final int localId)
	{
	  Route.PlotStoredRoute(localId, this, getActivity());
  } // onStoredRouteNow
    
	public void onClearRoute()
	{
	  Route.resetJourney();
	  routeSetter_.resetRoute();
	  path_.reset();
	  mapView().invalidate();
  } // onClearRoute
    
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
	  menu.add(0, R.string.ic_menu_directions, Menu.NONE, R.string.ic_menu_directions).setIcon(R.drawable.ic_menu_directions);
	  menu.add(0, R.string.ic_menu_saved_routes, Menu.NONE, R.string.ic_menu_saved_routes).setIcon(R.drawable.ic_menu_places);
    menu.add(0, R.string.ic_menu_route_number, Menu.NONE, R.string.ic_menu_route_number).setIcon(R.drawable.ic_menu_route_number);
    super.onCreateOptionsMenu(menu, inflater);
	} // onCreateOptionsMenu
    
	@Override
	public void onPrepareOptionsMenu(final Menu menu)
	{
	  final MenuItem i = menu.findItem(R.string.ic_menu_saved_routes);
		i.setVisible(Route.storedCount() != 0);
		super.onPrepareOptionsMenu(menu);
	} // onPrepareOptionsMenu
    
	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if(super.onOptionsItemSelected(item))
			return true;
		
		switch(item.getItemId())
		{
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
		
		if(requestCode == R.string.ic_menu_saved_routes)
		{
			final int localId = data.getIntExtra(CycleStreetsConstants.ROUTE_ID, 0);
			if(localId != 0)
				onStoredRouteNow(localId);
			return;
		} // if ...
		
		if(requestCode == R.string.ic_menu_directions)
		{
		  final List<GeoPoint> points = GeoIntent.getWaypoints(data);
			final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
			final int speed = data.getIntExtra(CycleStreetsConstants.EXTRA_ROUTE_SPEED, 
					                               CycleStreetsPreferences.speed());
			Route.PlotRoute(routeType, 
			                speed,
			                this, 
			                getActivity(),
                      points);
		} // if ...
		
		if(requestCode == R.string.ic_menu_route_number)
		{
		  final long routeNumber = data.getLongExtra(CycleStreetsConstants.EXTRA_ROUTE_NUMBER, -1);
		  final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
      final int speed = data.getIntExtra(CycleStreetsConstants.EXTRA_ROUTE_SPEED, 
                                         CycleStreetsPreferences.speed());
      
      Route.FetchRoute(routeType, routeNumber, speed, this, getActivity());
		} // if ...
	} // onActivityResult
    
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
	  startActivityForResult(intent, R.string.ic_menu_directions);
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
	  startActivityForResult(intent, R.string.ic_menu_route_number);
	} // doLaunchFetchRouteDialog
	
	private void launchStoredRoutesDialog()
	{
	  final Intent intent = new Intent(getActivity(), StoredRoutesActivity.class);
    startActivityForResult(intent, R.string.ic_menu_saved_routes);
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
	public void onNewJourney() 
	{
	  setJourneyPath(Route.segments(), Route.waypoints());
	  
	  mapView().getController().setCenter(Route.start());
	  mapView().postInvalidate();
	} // onNewJourney   
   
	private void setJourneyPath(final List<Segment> segments, final List<GeoPoint> waypoints)
	{
	  routeSetter_.setRoute(waypoints, !segments.isEmpty());	   
	  path_.setRoute(segments);
  } // setJourneyPath
} // class MapActivity