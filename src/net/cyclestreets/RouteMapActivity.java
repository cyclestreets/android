package net.cyclestreets;

import java.util.Iterator;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.R;
import net.cyclestreets.views.overlay.PathOfRouteOverlay;
import net.cyclestreets.views.overlay.RouteHighlightOverlay;
import net.cyclestreets.views.overlay.StoredRouteOverlay;
import net.cyclestreets.views.overlay.TapToRouteOverlay;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import uk.org.invisibility.cycloid.CycloidConstants;
import uk.org.invisibility.cycloid.GeoIntent;
import uk.org.invisibility.cycloid.RouteActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

 public class RouteMapActivity extends CycleMapActivity 
 							   implements TapToRouteOverlay.Callback, 
 							   			  StoredRouteOverlay.Callback,
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

        overlayPushTop(new StoredRouteOverlay(getApplicationContext(), this));
        
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
       	setJourneyPath(Route.points(), Route.from(), Route.to());
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
    
    public void onStoredRouteNow(final int routeId)
    {
    	Route.PlotRoute(routeId, this, this);
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
    	super.onCreateOptionsMenu(menu);
    	return true;
	} // onCreateOptionsMenu
    
	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		if(super.onMenuItemSelected(featureId, item))
			return true;
		
		if(item.getItemId() == R.string.ic_menu_directions)
		{
			launchRouteDialog();
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
		
		if(requestCode == R.string.ic_menu_directions)
		{
			// get start and finish points
			final GeoPoint placeFrom = new GeoPoint(data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LAT, 0),
					data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LONG, 0));
			final GeoPoint placeTo = new GeoPoint(data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_TO_LAT, 0),
					data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_TO_LONG, 0));
			final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
			final int speed = data.getIntExtra(CycleStreetsConstants.EXTRA_ROUTE_SPEED, 
					                           CycleStreetsPreferences.speed());
			Route.PlotRoute(routeType, 
							placeFrom, 
							placeTo,
							speed,
							this, 
							this);
		}
	} // onActivityResult
    
	private void launchRouteDialog()
	{
    	final Intent intent = new Intent(this, RouteActivity.class);
    	GeoIntent.setBoundingBoxInExtras(intent, mapView().getBoundingBox());
    	final Location lastFix = mapView().getLastFix();
        if (lastFix != null)
        {
        	intent.putExtra(CycloidConstants.GEO_LATITUDE, (int)(lastFix.getLatitude() * 1E6));
        	intent.putExtra(CycloidConstants.GEO_LONGITUDE, (int)(lastFix.getLongitude() * 1E6));
        } // if ...	
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
	   setJourneyPath(Route.points(), Route.from(), Route.to());
	   
	   mapView().getController().setCenter(Route.from());
	   mapView().postInvalidate();
   } // onNewJourney   
   
   private void setJourneyPath(final Iterator<GeoPoint> points, final GeoPoint from, final GeoPoint to)
   {
	   routeSetter_.setRoute(from, to, points.hasNext());
	   
	   path.setRoute(points);
   } // setJourneyPath
} // class MapActivity