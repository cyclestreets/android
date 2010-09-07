package uk.org.invisibility.cycloid;

import net.cyclestreets.R;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.MyLocationOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewPathOverlay;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
 * TODO constant refreshes after a my location
 * TODO add option for shortest plan type
 * TODO active icons for plan types
 * TODO implement search?
 * TODO option to toggle cycle / normal map on and off?
 * TODO geocoding postcodes doesn't work very well
 * TODO geocode in progress indicator in route to/from
 */

 public class MapActivity extends Activity implements CycloidConstants
 {
    private static final int MENU_MY_LOCATION = Menu.FIRST;
    private static final int MENU_ROUTE = MENU_MY_LOCATION + 1;
    private static final int MENU_ABOUT = MENU_ROUTE + 1;

	private static final int DIALOG_ABOUT_ID = 1;
	
	public static OpenStreetMapView map; 
	private OpenStreetMapViewPathOverlay path;
	private MyLocationOverlay location;
	private ResourceProxy proxy;
	private SharedPreferences prefs;
	
    @Override
    public void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        proxy = new CycloidResourceProxy(getApplicationContext());
        prefs = getSharedPreferences(PREFS_APP_KEY, MODE_PRIVATE);

        map = new OpenStreetMapView
        (
    		this,
    		OpenStreetMapRendererInfo.values()[prefs.getInt(PREFS_APP_RENDERER, MAPTYPE.ordinal())]
        );
        map.setResourceProxy(proxy);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(prefs.getInt(PREFS_APP_ZOOM_LEVEL, 14));
        map.scrollTo(prefs.getInt(PREFS_APP_SCROLL_X, 0), prefs.getInt(PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */

        location = new MyLocationOverlay(this.getBaseContext(), map, proxy);
        map.getOverlays().add(location);
        
        path = new MapActivityPathOverlay(0x80ff0000, proxy);
        map.getOverlays().add(path);
        
        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(map, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.setContentView(rl);

        /*
         * If a route was supplied with the launching intent then display it 
         */
		Intent intent = getIntent();
		RouteResult route = intent.getParcelableExtra("route");
		if (route != null)
		{
			boolean first = true;
			for (GeoPoint p : route.getCoords())
			{
				if (first)
				{
					map.getController().setCenter(p);
					first = false;
				}
				path.addPoint(p.getLatitudeE6(), p.getLongitudeE6());
			}
		}
    }

    @Override
    protected void onPause()
    {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(PREFS_APP_RENDERER, map.getRenderer().ordinal());
        edit.putInt(PREFS_APP_SCROLL_X, map.getScrollX());
        edit.putInt(PREFS_APP_SCROLL_Y, map.getScrollY());
        edit.putInt(PREFS_APP_ZOOM_LEVEL, map.getZoomLevel());
        edit.putBoolean(PREFS_APP_FOLLOW_LOCATION, location.isLocationFollowEnabled());
        edit.commit();

        Log.w(LOGTAG, "X: " + map.getScrollX() + " Y: " + map.getScrollY() + " Z: " + map.getZoomLevel());
        
        location.disableMyLocation();     
        super.onPause();
    }

    @Override
    protected void onResume()
    {
    	super.onResume();
        map.setRenderer(OpenStreetMapRendererInfo.values()[prefs.getInt(PREFS_APP_RENDERER, MAPTYPE.ordinal())]);
        this.location.followLocation(prefs.getBoolean(PREFS_APP_FOLLOW_LOCATION, true));
    }
    
    @Override
	public boolean onCreateOptionsMenu(final Menu pMenu)
    {
    	pMenu.add(0, MENU_MY_LOCATION, Menu.NONE, R.string.my_location).setIcon(android.R.drawable.ic_menu_mylocation);
    	pMenu.add(0, MENU_ROUTE, Menu.NONE, R.string.route).setIcon(android.R.drawable.ic_menu_directions);
    	pMenu.add(0, MENU_ABOUT, Menu.NONE, R.string.about).setIcon(android.R.drawable.ic_menu_info_details);
    	return true;
	}
    	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		Location lastFix;
		
		switch (item.getItemId())
		{
            case MENU_MY_LOCATION:
                location.followLocation(true);
                location.enableMyLocation();
                lastFix = location.getLastFix();
                if (lastFix != null)
                    map.getController().setCenter(new GeoPoint(lastFix));
                return true;

            case MENU_ROUTE:
            	Intent intent = new Intent(this, RouteActivity.class);
            	BoundingBoxE6 bounds = map.getDrawnBoundingBoxE6();
            	GeoIntent.setBoundingBoxInExtras(intent, bounds);
                lastFix = location.getLastFix();
                if (lastFix != null)
                {
                	intent.putExtra(GEO_LATITUDE, (int)(lastFix.getLatitude() * 1E6));
                	intent.putExtra(GEO_LONGITUDE, (int)(lastFix.getLongitude() * 1E6));
                }	
                startActivity(intent);
                return true;
                                
			case MENU_ABOUT:
				showDialog(DIALOG_ABOUT_ID);
				return true;
		
		}
		return false;
	}
	
   @Override
   protected Dialog onCreateDialog(int id)
   {
        Dialog dialog;

        switch (id)
        {
        case DIALOG_ABOUT_ID:
        	dialog = new AlertDialog.Builder(MapActivity.this)
            .setIcon(R.drawable.icon)
            .setTitle(R.string.app_name)
            .setMessage(R.string.about_message)
            .setPositiveButton
            (
        		"OK",
        		new DialogInterface.OnClickListener()
	            {
	                @Override
	                public void onClick(DialogInterface dialog, int whichButton) {}
	            }
        	).create();
        	break;

        default:
            dialog = null;
            break;
        }
        return dialog;
    }
   
   @Override
   public boolean onTrackballEvent(MotionEvent event)
   {
       return map.onTrackballEvent(event);
   }
  
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
       if (event.getAction() == MotionEvent.ACTION_MOVE)
           location.followLocation(false);
       return super.onTouchEvent(event);
   }
   
   private class MapActivityPathOverlay extends OpenStreetMapViewPathOverlay
   {
       public MapActivityPathOverlay(final int colour, final ResourceProxy pResourceProxy)
       {
           super(colour, pResourceProxy);
           mPaint.setStrokeWidth(6.0f);
       }

   };
}