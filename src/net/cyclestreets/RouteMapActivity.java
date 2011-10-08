package net.cyclestreets;

import java.util.Iterator;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.R;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.views.overlay.POIOverlay;
import net.cyclestreets.views.overlay.PathOfRouteOverlay;
import net.cyclestreets.views.overlay.RouteHighlightOverlay;
import net.cyclestreets.views.overlay.TapToRouteOverlay;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class RouteMapActivity extends CycleMapActivity 
                              implements TapToRouteOverlay.Callback, 
                                         Route.Callback
{
	private PathOfRouteOverlay path_;
	private TapToRouteOverlay routeSetter_;
	private POIOverlay poiOverlay_;
	
	@Override
	public void onCreate(final Bundle saved)
	{
	  super.onCreate(saved);

	  overlayPushBottom(new RouteHighlightOverlay(getApplicationContext(), mapView()));
        
    poiOverlay_ = new POIOverlay(getApplicationContext(), mapView());
    overlayPushBottom(poiOverlay_);

    path_ = new PathOfRouteOverlay(getApplicationContext());
	  overlayPushBottom(path_);
	  
	  routeSetter_ = new TapToRouteOverlay(getApplicationContext(), mapView(), this);
	  overlayPushTop(routeSetter_);
  } // onCreate

	@Override
	protected void onPause()
	{
	  Route.setTerminals(routeSetter_.getStart(), routeSetter_.getFinish());
	  super.onPause();
  } // onPause

	@Override
	protected void onResume()
	{
	  super.onResume();
	  setJourneyPath(Route.points(), Route.start(), Route.finish());
  } // onResume
     
	public void onRouteNow(final GeoPoint start, final GeoPoint end)
	{
	  Route.PlotRoute(CycleStreetsPreferences.routeType(), 
	                  start, 
	                  end,
	                  CycleStreetsPreferences.speed(),
	                  this, 
	                  this);
	} // onRouteNow

	public void onRouteNow(int itinerary)
	{
	  Route.FetchRoute(CycleStreetsPreferences.routeType(),
	                   itinerary, 
	                   CycleStreetsPreferences.speed(), 
	                   this,
	                   this);
	}
	
	public void reRouteNow(final String plan)
	{
	  Route.RePlotRoute(plan,
	                    this, 
	                    this);
	} // reRouteNow

	public void onStoredRouteNow(final int localId)
	{
	  Route.PlotStoredRoute(localId, this, this);
  } // onStoredRouteNow
    
	public void onClearRoute()
	{
	  Route.resetJourney();
	  routeSetter_.resetRoute();
	  path_.clearPath();
	  mapView().invalidate();
  } // onClearRoute
    
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
	  menu.add(0, R.string.ic_menu_directions, Menu.NONE, R.string.ic_menu_directions).setIcon(R.drawable.ic_menu_directions);
	  menu.add(0, R.string.ic_menu_saved_routes, Menu.NONE, R.string.ic_menu_saved_routes).setIcon(R.drawable.ic_menu_places);
	  menu.add(0, R.string.ic_menu_route_number, Menu.NONE, R.string.ic_menu_route_number).setIcon(R.drawable.ic_menu_route_number);
	  super.onCreateOptionsMenu(menu);
	  return true;
	} // onCreateOptionsMenu
    
	@Override
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
	  final MenuItem i = menu.findItem(R.string.ic_menu_saved_routes);
		i.setVisible(Route.storedCount() != 0);
		super.onPrepareOptionsMenu(menu);
		return true;
	} // onPrepareOptionsMenu
    
	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		if(super.onMenuItemSelected(featureId, item))
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
  {
	  super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode != RESULT_OK)
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
			// get start and finish points
			final GeoPoint placeFrom = GeoIntent.getGeoPoint(data, "FROM");
			final GeoPoint placeTo = GeoIntent.getGeoPoint(data, "TO");
			final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
			final int speed = data.getIntExtra(CycleStreetsConstants.EXTRA_ROUTE_SPEED, 
					                               CycleStreetsPreferences.speed());
			Route.PlotRoute(routeType, 
			                placeFrom, 
			                placeTo,
			                speed,
			                this, 
			                this);
		} // if ...
		
		if(requestCode == R.string.ic_menu_route_number)
		{
		  final long routeNumber = data.getLongExtra(CycleStreetsConstants.EXTRA_ROUTE_NUMBER, -1);
		  final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
      final int speed = data.getIntExtra(CycleStreetsConstants.EXTRA_ROUTE_SPEED, 
                                         CycleStreetsPreferences.speed());
      
      Route.FetchRoute(routeType, routeNumber, speed, this, this);
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
	  final Intent intent = new Intent(this, RouteByAddressActivity.class);
	  GeoIntent.setBoundingBox(intent, mapView().getBoundingBox());
	  final Location lastFix = mapView().getLastFix();
	  GeoIntent.setLocation(intent, lastFix);	
	  GeoIntent.setGeoPoint(intent, "START", routeSetter_.getStart());
	  GeoIntent.setGeoPoint(intent, "FINISH", routeSetter_.getFinish());
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
	  final Intent intent = new Intent(this, RouteNumberActivity.class);
	  startActivityForResult(intent, R.string.ic_menu_route_number);
	} // doLaunchFetchRouteDialog
	
	private void launchStoredRoutesDialog()
	{
	  final Intent intent = new Intent(this, StoredRoutesActivity.class);
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
	public void onBackPressed()
	{
		if(!routeSetter_.onBackButton())
			super.onBackPressed();
	} // onBackPressed
	
	@Override
	public void onNewJourney() 
	{
	  setJourneyPath(Route.points(), Route.start(), Route.finish());
	  
	  mapView().getController().setCenter(Route.start());
	  mapView().postInvalidate();
	} // onNewJourney   
   
	private void setJourneyPath(final Iterator<GeoPoint> points, final GeoPoint start, final GeoPoint finish)
	{
	  routeSetter_.setRoute(start, finish, points.hasNext());	   
	  path_.setRoute(points);
  } // setJourneyPath
} // class MapActivity