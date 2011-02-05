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

import org.osmdroid.util.BoundingBoxE6;
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
	private RouteHighlightOverlay highlight;
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
        map.getOverlays().add(new ZoomButtonsOverlay(getApplicationContext(), map));
        
        highlight = new RouteHighlightOverlay(getApplicationContext(), map);
        map.getOverlays().add(highlight);
        
        location = new LocationOverlay(getApplicationContext(), map, this);
        map.getOverlays().add(location);

        map.getOverlays().add(new TapOverlay(getApplicationContext(), map));
        
        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(map, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.setContentView(rl);
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
     
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CycleStreetsConstants.ACTIVITY_GET_ENDPOINTS) {
			if (resultCode == RESULT_OK) {
				// get start and finish points
				GeoPoint placeFrom = new GeoPoint(data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LAT, 0),
												  data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_FROM_LONG, 0));
				GeoPoint placeTo = new GeoPoint(data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_TO_LAT, 0),
						                        data.getIntExtra(CycleStreetsConstants.EXTRA_PLACE_TO_LONG, 0));
				String routeType = data.getStringExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE);
				Log.d(getClass().getSimpleName(), "got places: " + placeFrom + "->" + placeTo + " " + routeType);
				
				// calculate journey
				RoutingTask.PlotRoute(routeType, placeFrom, placeTo, this, this);
			}
		}
	} // onActivityResult
    
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
    	return true;
	} // onCreateOptionsMenu
    	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
            case R.string.ic_menu_directions:
            {
            	final Intent intent = new Intent(this, RouteActivity.class);
            	final BoundingBoxE6 bounds = map.getBoundingBox();
            	GeoIntent.setBoundingBoxInExtras(intent, bounds);
            	final Location lastFix = location.getLastFix();
                if (lastFix != null)
                {
                	intent.putExtra(CycloidConstants.GEO_LATITUDE, (int)(lastFix.getLatitude() * 1E6));
                	intent.putExtra(CycloidConstants.GEO_LONGITUDE, (int)(lastFix.getLongitude() * 1E6));
                }	
                startActivityForResult(intent, CycleStreetsConstants.ACTIVITY_GET_ENDPOINTS);
                return true;
            } // case
            case R.string.ic_menu_mylocation:
            	location.enableLocation(!location.isMyLocationEnabled());
            	break;
		} // switch
		return false;
	} // onMenuItemSelected
	
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
   public void onNewJourney() {
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