package net.cyclestreets;

import java.util.Iterator;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.RoutingTask;
import net.cyclestreets.R;
import net.cyclestreets.overlay.LocationOverlay;
import net.cyclestreets.overlay.PathOfRouteOverlay;
import net.cyclestreets.overlay.RouteHighlightOverlay;
import net.cyclestreets.overlay.TapOverlay;
import net.cyclestreets.overlay.ZoomButtonsOverlay;
import net.cyclestreets.planned.Route;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import uk.org.invisibility.cycloid.CycloidConstants;
import uk.org.invisibility.cycloid.GeoIntent;
import uk.org.invisibility.cycloid.RouteActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

 public class MapActivity extends Activity implements LocationOverlay.Callback, RoutingTask.Callback
 {
	private MapView map; 
	
	private PathOfRouteOverlay path;
	private LocationOverlay location;
	
	private SharedPreferences prefs;
	
    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        prefs = getSharedPreferences(CycloidConstants.PREFS_APP_KEY, MODE_PRIVATE);

		map = new MapView(this, null);
		map.setTileSource(mapRenderer());
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getController().setZoom(prefs.getInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, 14));
        map.scrollTo(prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_X, 0), prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */

        path = new PathOfRouteOverlay(getApplicationContext());
        map.getOverlays().add(path);
        
        map.getOverlays().add(new RouteHighlightOverlay(getApplicationContext(), map));
        
        map.getOverlays().add(new ZoomButtonsOverlay(getApplicationContext(), map));

        location = new LocationOverlay(getApplicationContext(), map, this);
        map.getOverlays().add(location);

        map.getOverlays().add(new TapOverlay(getApplicationContext(), map));
        
        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(map, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        setContentView(rl);
    } // onCreate

    @Override
    protected void onPause()
    {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(CycloidConstants.PREFS_APP_SCROLL_X, map.getScrollX());
        edit.putInt(CycloidConstants.PREFS_APP_SCROLL_Y, map.getScrollY());
        edit.putInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, map.getZoomLevel());
        edit.putBoolean(CycloidConstants.PREFS_APP_MY_LOCATION, location.isMyLocationEnabled());
        edit.putBoolean(CycloidConstants.PREFS_APP_FOLLOW_LOCATION, location.isLocationFollowEnabled());
        edit.commit();

        Log.w(CycloidConstants.LOGTAG, "X: " + map.getScrollX() + " Y: " + map.getScrollY() + " Z: " + map.getZoomLevel());
        
        location.disableMyLocation();
        
        Route.setTerminals(location.getStart(), location.getEnd());
        super.onPause();
    } // onPause

    @Override
    protected void onResume()
    {
    	super.onResume();

        location.enableLocation(prefs.getBoolean(CycloidConstants.PREFS_APP_MY_LOCATION, false));
        location.followLocation(prefs.getBoolean(CycloidConstants.PREFS_APP_FOLLOW_LOCATION, false));
        
        map.getScroller().abortAnimation();
        
        map.scrollTo(prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_X, 0), prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */
        map.getController().setZoom(prefs.getInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, 14));

       	setJourneyPath(Route.points(), Route.from(), Route.to());
    } // onResume
     
    public void onRouteNow(final GeoPoint start, final GeoPoint end)
    {
    	RoutingTask.PlotRoute(CycleStreetsConstants.PLAN_BALANCED, start, end, this, this);
    } // onRouteNow
    
    public void onClearRoute()
    {
    	Route.resetJourney();
    	location.resetRoute();
    	path.clearPath();
    	map.invalidate();
    } // onClearRoute
    
    @Override
	public boolean onCreateOptionsMenu(final Menu menu)
    {
    	menu.add(0, R.string.ic_menu_directions, Menu.NONE, R.string.ic_menu_directions).setIcon(R.drawable.ic_menu_directions);
    	menu.add(0, R.string.ic_menu_mylocation, Menu.NONE, R.string.ic_menu_mylocation).setIcon(R.drawable.ic_menu_mylocation);
    	menu.add(0, R.string.ic_menu_findplace, Menu.NONE, R.string.ic_menu_findplace).setIcon(R.drawable.ic_menu_search);
    	return true;
	} // onCreateOptionsMenu
    	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
            case R.string.ic_menu_directions:
            	launchRouteDialog();
                return true;
            case R.string.ic_menu_findplace:
            	launchFindDialog();
            	return true;
            case R.string.ic_menu_mylocation:
            	location.enableLocation(!location.isMyLocationEnabled());
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
				location.centreOn(place);
				map.invalidate();
			}
			break;
		} // switch
	} // onActivityResult
    
	private void launchRouteDialog()
	{
    	final Intent intent = new Intent(this, RouteActivity.class);
    	GeoIntent.setBoundingBoxInExtras(intent, map.getBoundingBox());
    	final Location lastFix = location.getLastFix();
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
    	GeoIntent.setBoundingBoxInExtras(intent, map.getBoundingBox());
    	startActivityForResult(intent, R.string.ic_menu_findplace);
	} // launchFindDialog
	
	@Override 
	public void onBackPressed()
	{
		if(!location.onBackButton())
			super.onBackPressed();
	} // onBackPressed
   
   @Override
   public boolean onTrackballEvent(MotionEvent event)
   {
       return map.onTrackballEvent(event);
   } // onTrackballEvent
  
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
       if (event.getAction() == MotionEvent.ACTION_MOVE)
           location.followLocation(false);
       return super.onTouchEvent(event);
   } // onTouchEvent
   
   @Override
   public void onNewJourney() 
   {
	   setJourneyPath(Route.points(), Route.from(), Route.to());
	   
	   map.getController().setCenter(Route.from());
	   map.postInvalidate();
   } // onNewJourney   
   
   private void setJourneyPath(final Iterator<GeoPoint> points, final GeoPoint from, final GeoPoint to)
   {
	   location.setRoute(from, to, points.hasNext());
	   
	   path.setRoute(points);
   } // setJourneyPath
   
   private ITileSource mapRenderer()
   {
	   return TileSourceFactory.getTileSource(prefs.getString(CycloidConstants.PREFS_APP_RENDERER, CycloidConstants.DEFAULT_MAPTYPE));
   } // mapRenderer
} // class MapActivity