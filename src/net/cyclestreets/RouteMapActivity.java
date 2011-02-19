package net.cyclestreets;

import java.util.Iterator;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.RoutingTask;
import net.cyclestreets.R;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.views.overlay.PathOfRouteOverlay;
import net.cyclestreets.views.overlay.RouteHighlightOverlay;
import net.cyclestreets.views.overlay.TapToRouteOverlay;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;

import uk.org.invisibility.cycloid.CycloidConstants;
import uk.org.invisibility.cycloid.GeoIntent;
import uk.org.invisibility.cycloid.RouteActivity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

 public class RouteMapActivity extends Activity implements TapToRouteOverlay.Callback, RoutingTask.Callback
 {
	private CycleMapView map_; 
	
	private PathOfRouteOverlay path;
	private TapToRouteOverlay routeSetter_;
	
    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);

		map_ = new CycleMapView(this, "route");

        map_.overlayPushBottom(new RouteHighlightOverlay(getApplicationContext(), map_));
        
        path = new PathOfRouteOverlay(getApplicationContext());
        map_.overlayPushBottom(path);

        routeSetter_ = new TapToRouteOverlay(getApplicationContext(), map_, this);
        map_.overlayPushTop(routeSetter_);
        
        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(map_, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        setContentView(rl);
    } // onCreate

    @Override
    protected void onPause()
    {
    	map_.onPause();
        Route.setTerminals(routeSetter_.getStart(), routeSetter_.getEnd());
        super.onPause();
    } // onPause

    @Override
    protected void onResume()
    {
    	super.onResume();

    	map_.onResume();
       	setJourneyPath(Route.points(), Route.from(), Route.to());
    } // onResume
     
    public void onRouteNow(final GeoPoint start, final GeoPoint end)
    {
    	RoutingTask.PlotRoute(CycleStreetsPreferences.routeType(), start, end, this, this);
    } // onRouteNow
    
    public void onClearRoute()
    {
    	Route.resetJourney();
    	routeSetter_.resetRoute();
    	path.clearPath();
    	map_.invalidate();
    } // onClearRoute
    
    @Override
	public boolean onCreateOptionsMenu(final Menu menu)
    {
    	map_.onCreateOptionsMenu(menu);
    	menu.add(0, R.string.ic_menu_directions, Menu.NONE, R.string.ic_menu_directions).setIcon(R.drawable.ic_menu_directions);
    	menu.add(0, R.string.ic_menu_findplace, Menu.NONE, R.string.ic_menu_findplace).setIcon(R.drawable.ic_menu_search);
    	return true;
	} // onCreateOptionsMenu
    	
	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		if(map_.onMenuItemSelected(featureId, item))
			return true;
		
		switch (item.getItemId())
		{
            case R.string.ic_menu_directions:
            	launchRouteDialog();
                return true;
            case R.string.ic_menu_findplace:
            	launchFindDialog();
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
		
		switch(requestCode)
		{
		case R.string.ic_menu_directions:
			{
				// get start and finish points
				final GeoPoint placeFrom = new GeoPoint(data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LAT, 0),
														data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LONG, 0));
				final GeoPoint placeTo = new GeoPoint(data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_TO_LAT, 0),
													  data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_TO_LONG, 0));
				final String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
				RoutingTask.PlotRoute(routeType, placeFrom, placeTo, this, this);
			}
			break;
		case R.string.ic_menu_findplace:
			{
				final GeoPoint place = new GeoPoint(data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LAT, 0),
													data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LONG, 0));
				// we're in the wrong thread, so pop this away for later and force a refresh
				map_.centreOn(place);
			}
			break;
		} // switch
	} // onActivityResult
    
	private void launchRouteDialog()
	{
    	final Intent intent = new Intent(this, RouteActivity.class);
    	GeoIntent.setBoundingBoxInExtras(intent, map_.getBoundingBox());
    	final Location lastFix = map_.getLastFix();
        if (lastFix != null)
        {
        	intent.putExtra(CycloidConstants.GEO_LATITUDE, (int)(lastFix.getLatitude() * 1E6));
        	intent.putExtra(CycloidConstants.GEO_LONGITUDE, (int)(lastFix.getLongitude() * 1E6));
        } // if ...	
        startActivityForResult(intent, R.string.ic_menu_directions);
	} // launchRouteDialog
	
	private void launchFindDialog()
	{
		final Intent intent = new Intent(this, FindPlaceActivity.class);
    	GeoIntent.setBoundingBoxInExtras(intent, map_.getBoundingBox());
    	startActivityForResult(intent, R.string.ic_menu_findplace);
	} // launchFindDialog
	
	@Override 
	public void onBackPressed()
	{
		if(!routeSetter_.onBackButton())
			super.onBackPressed();
	} // onBackPressed
	
	@Override
	public boolean onSearchRequested()
	{
		launchFindDialog();
		return true;
	} // onSearchRequested
   
   @Override
   public boolean onTrackballEvent(MotionEvent event)
   {
       return map_.onTrackballEvent(event);
   } // onTrackballEvent
  
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
       if (event.getAction() == MotionEvent.ACTION_MOVE)
           map_.disableFollowLocation();
       return super.onTouchEvent(event);
   } // onTouchEvent
   
   @Override
   public void onNewJourney() 
   {
	   setJourneyPath(Route.points(), Route.from(), Route.to());
	   
	   map_.getController().setCenter(Route.from());
	   map_.postInvalidate();
   } // onNewJourney   
   
   private void setJourneyPath(final Iterator<GeoPoint> points, final GeoPoint from, final GeoPoint to)
   {
	   routeSetter_.setRoute(from, to, points.hasNext());
	   
	   path.setRoute(points);
   } // setJourneyPath
} // class MapActivity