package uk.org.invisibility.cycloid;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.RoutingTask;
import net.cyclestreets.R;
import net.cyclestreets.api.Journey;
import net.cyclestreets.api.Marker;
import net.cyclestreets.overlay.LocationOverlay;
import net.cyclestreets.overlay.PathOfRouteOverlay;
import net.cyclestreets.planned.Route;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

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

/*
 * TODO update person picture
 * TODO add option for shortest plan type
 * TODO active icons for plan types
 * TODO implement search?
 * TODO option to toggle cycle / normal map on and off?
 * TODO geocoding postcodes doesn't work very well
 * TODO geocode in progress indicator in route to/from
 */

 public class MapActivity extends Activity implements LocationOverlay.Callback, RoutingTask.Callback
 {
    private static final int MENU_ROUTE = Menu.FIRST;

	private MapView map; 
	private PathOfRouteOverlay path;
	private LocationOverlay location;
	private ResourceProxy proxy;
	private SharedPreferences prefs;
	
    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        proxy = new CycloidResourceProxy(getApplicationContext());
        prefs = getSharedPreferences(CycloidConstants.PREFS_APP_KEY, MODE_PRIVATE);

		map = new MapView(this, null);
		map.setTileSource(mapRenderer());
        map.setResourceProxy(proxy);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(prefs.getInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, 14));
        map.scrollTo(prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_X, 0), prefs.getInt(CycloidConstants.PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */

        path = new PathOfRouteOverlay(proxy);
        map.getOverlays().add(path);

        location = new LocationOverlay(this.getBaseContext(), map, this, proxy);
        map.getOverlays().add(location);
        
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
        
        Route.onNewJourney(Route.journey(), location.getStart(), location.getEnd());
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

       	setJourneyPath(Route.journey(), Route.from(), Route.to());
    } // onResume
     
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
	}

    public void onRouteNow(final GeoPoint start, final GeoPoint end)
    {
    	RoutingTask.PlotRoute(CycleStreetsConstants.PLAN_BALANCED, start, end, this, this);
    } // onRouteNow
    
    public void onClearRoute()
    {
    	location.resetRoute();
    	Route.resetJourney();
    	path.clearPath();
    	map.invalidate();
    } // onClearRoute
    
    @Override
	public boolean onCreateOptionsMenu(final Menu pMenu)
    {
    	pMenu.add(0, MENU_ROUTE, Menu.NONE, R.string.route).setIcon(android.R.drawable.ic_menu_directions);
    	return true;
	} // onCreateOptionsMenu
    	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
            case MENU_ROUTE:
            {
            	Intent intent = new Intent(this, RouteActivity.class);
            	BoundingBoxE6 bounds = map.getBoundingBox();
            	GeoIntent.setBoundingBoxInExtras(intent, bounds);
                Location lastFix = location.getLastFix();
                if (lastFix != null)
                {
                	intent.putExtra(CycloidConstants.GEO_LATITUDE, (int)(lastFix.getLatitude() * 1E6));
                	intent.putExtra(CycloidConstants.GEO_LONGITUDE, (int)(lastFix.getLongitude() * 1E6));
                }	
                startActivityForResult(intent, CycleStreetsConstants.ACTIVITY_GET_ENDPOINTS);
                return true;
            }
		}
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
	   Journey journey = Route.journey();

	   setJourneyPath(journey, Route.from(), Route.to());
	   
	   map.getController().setCenter(path.pathStart());
	   map.postInvalidate();
   } // onNewJourney   
   
   private void setJourneyPath(final Journey journey, final GeoPoint from, final GeoPoint to)
   {
	   location.setRoute(from, to, journey != null);
	   
	   path.clearPath();

	   if(journey == null)
		   return;

	   for (Marker marker: journey.markers) {
		   if (marker.type.equals("route")) {
			   String[] coords = marker.coordinates.split(" ");
			   for (String coord : coords) {
				   String[] xy = coord.split(",");
				   GeoPoint p = new GeoPoint(Double.parseDouble(xy[1]), Double.parseDouble(xy[0]));
				   path.addPoint(p);
			   }
		   } // if ...
	   } // for ...
   } // setJourneyPath

   private ITileSource mapRenderer()
   {
	   return TileSourceFactory.getTileSource(prefs.getString(CycloidConstants.PREFS_APP_RENDERER, CycloidConstants.DEFAULT_MAPTYPE));
   } // mapRenderer
} // class MapActivity