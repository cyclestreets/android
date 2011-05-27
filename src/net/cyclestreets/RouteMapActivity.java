package net.cyclestreets;

import java.util.Iterator;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.R;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.GeoIntent;
 	import net.cyclestreets.views.overlay.PathOfRouteOverlay;
import net.cyclestreets.views.overlay.RouteHighlightOverlay;
import net.cyclestreets.views.overlay.TapToRouteOverlay;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import uk.org.invisibility.cycloid.RouteActivity;

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
	private PathOfRouteOverlay path;
	private TapToRouteOverlay routeSetter_;
	
    @Override
    public void onCreate(final Bundle saved)
    {
        super.onCreate(saved);

        overlayPushBottom(new RouteHighlightOverlay(getApplicationContext(), mapView()));
        
        path = new PathOfRouteOverlay(getApplicationContext());
        overlayPushBottom(path);

        routeSetter_ = new TapToRouteOverlay(getApplicationContext(), mapView(), this);
        overlayPushTop(routeSetter_);
    } // onCreate

    @Override
    protected void onPause()
    {
        Route.setTerminals(routeSetter_.getStart(), routeSetter_.getEnd());
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
    
    public void reRouteNow(final String plan)
    {
    	Route.RePlotRoute(plan,
    					  this, 
    					  this);
    } // reRouteNow

    public void onStoredRouteNow(final int localId)
    {
    	Route.PlotRoute(localId, this, this);
    } // onStoredRouteNow
    
    public void onClearRoute()
    {
    	Route.resetJourney();
    	routeSetter_.resetRoute();
    	path.clearPath();
    	mapView().invalidate();
    } // onClearRoute
    
    @Override
	public boolean onCreateOptionsMenu(final Menu menu)
    {
    	menu.add(0, R.string.ic_menu_directions, Menu.NONE, R.string.ic_menu_directions).setIcon(R.drawable.ic_menu_directions);
    	menu.add(0, R.string.ic_menu_saved_routes, Menu.NONE, R.string.ic_menu_saved_routes).setIcon(R.drawable.ic_menu_places);
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
		
		if(item.getItemId() == R.string.ic_menu_directions)
		{
			if(Route.available() && CycleStreetsPreferences.confirmNewRoute())
				MessageBox.YesNo(mapView(),
								 "Start a new route?",
								 new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0, int arg1) {
										launchRouteDialog();
									}
	        				 	 });
			else
				launchRouteDialog();
			return true;
		} // if ...
		
		if(item.getItemId() == R.string.ic_menu_saved_routes)
		{
			final Intent intent = new Intent(this, StoredRoutesActivity.class);
    		startActivityForResult(intent, R.string.ic_menu_saved_routes);
    		return true;
		} // if ...
		
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
	} // onActivityResult
    
	private void launchRouteDialog()
	{
    	final Intent intent = new Intent(this, RouteActivity.class);
    	GeoIntent.setBoundingBox(intent, mapView().getBoundingBox());
    	final Location lastFix = mapView().getLastFix();
    	GeoIntent.setLocation(intent, lastFix);	
        startActivityForResult(intent, R.string.ic_menu_directions);
	} // launchRouteDialog

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
	   
	   path.setRoute(points);
   } // setJourneyPath
} // class MapActivity