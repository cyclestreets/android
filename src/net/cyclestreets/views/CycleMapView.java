package net.cyclestreets.views;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.views.overlay.LocationOverlay;
import net.cyclestreets.views.overlay.ControllerOverlay;
import net.cyclestreets.views.overlay.ZoomButtonsOverlay;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import uk.org.invisibility.cycloid.CycloidConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;

public class CycleMapView extends MapView
{
	private ITileSource renderer_;
	private final SharedPreferences prefs_;
	private final ControllerOverlay controllerOverlay_;
	private final LocationOverlay location_;
	private final int overlayBottomIndex_;
	
	private GeoPoint centreOn_ = null;

	public CycleMapView(final Context context, final String name)
	{
		super(context, null);

		prefs_ = context.getSharedPreferences(CycloidConstants.PREFS_APP_KEY+"."+name, Context.MODE_PRIVATE);
		
        setBuiltInZoomControls(false);
        setMultiTouchControls(true);
	
        overlayBottomIndex_ = getOverlays().size();
        
        getOverlays().add(new ZoomButtonsOverlay(context, this));

        location_ = new LocationOverlay(context, this);
        getOverlays().add(location_);

        controllerOverlay_ = new ControllerOverlay(context, this);
        getOverlays().add(controllerOverlay_);
        
        onResume();
	} // CycleMapView
	
	public Overlay overlayPushBottom(final Overlay overlay)
	{
		getOverlays().add(overlayBottomIndex_, overlay);
		return overlay;
	} // overlayPushBottom
	
	public Overlay overlayPushTop(final Overlay overlay)
	{
		// keep TapOverlay on top
		int front = getOverlays().size()-1;
		getOverlays().add(front, overlay);
		return overlay;
	} // overlayPushFront
	
	/////////////////////////////////////////
	// save/restore
	public void onPause()
	{
        final SharedPreferences.Editor edit = prefs_.edit();
        edit.putInt(CycloidConstants.PREFS_APP_SCROLL_X, getScrollX());
        edit.putInt(CycloidConstants.PREFS_APP_SCROLL_Y, getScrollY());
        edit.putInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, getZoomLevel());
        edit.putBoolean(CycloidConstants.PREFS_APP_MY_LOCATION, location_.isMyLocationEnabled());
        edit.putBoolean(CycloidConstants.PREFS_APP_FOLLOW_LOCATION, location_.isFollowLocationEnabled());
        edit.commit();

        disableMyLocation();
	} // onPause

	public void onResume()
	{
		ITileSource tileSource = mapRenderer();
		if(!tileSource.equals(renderer_))
		{
			renderer_ = tileSource;
			setTileSource(renderer_);
		} // if ...
		
		
        location_.enableLocation(pref(CycloidConstants.PREFS_APP_MY_LOCATION, false));
        if(pref(CycloidConstants.PREFS_APP_FOLLOW_LOCATION, false))
        	location_.enableFollowLocation();
        else
        	location_.disableFollowLocation();
        
        getScroller().abortAnimation();
        
        scrollTo(pref(CycloidConstants.PREFS_APP_SCROLL_X, 0), 
        		 pref(CycloidConstants.PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */
        getController().setZoom(pref(CycloidConstants.PREFS_APP_ZOOM_LEVEL, 14));
	} // onResume 
	
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		return controllerOverlay_.onCreateOptionsMenu(menu);		
	} // onCreateOptionsMenu
	
	public boolean onPrepareOptionsMenu(final Menu menu)
	{
		return controllerOverlay_.onPrepareOptionsMenu(menu);
	} // onPrepareOptionsMenu
	
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
	{
		return controllerOverlay_.onMenuItemSelected(featureId, item);
	} // onMenuItemSelected
	
	/////////////////////////////////////////
	// location
	public Location getLastFix() { return location_.getLastFix(); }
	public void toggleMyLocation() { location_.enableLocation(!location_.isMyLocationEnabled()); }
	public void disableMyLocation() { location_.disableMyLocation(); }
	public void disableFollowLocation() { location_.disableFollowLocation(); }

	public void enableAndFollowLocation() { location_.enableAndFollowLocation(true); }
	
	///////////////////////////////////////////////////////
	public void centreOn(final GeoPoint place)
	{
		centreOn_ = place;
		invalidate();
	} // centreOn
		
	@Override
	protected void dispatchDraw(final Canvas canvas)
	{
		if(centreOn_  != null)
		{
			getController().animateTo(new GeoPoint(centreOn_));
			centreOn_ = null;
			return;
		} // if ..
		
		super.dispatchDraw(canvas);
	} // dispatchDraw

	///////////////////////////////////////////////////////
	private int pref(final String key, int defValue)
	{
		return prefs_.getInt(key, defValue);
	} // pref
	private boolean pref(final String key, boolean defValue)
	{
		return prefs_.getBoolean(key, defValue);
	} // pref
	
	private ITileSource mapRenderer()
	{		
		try { 
			return TileSourceFactory.getTileSource(CycleStreetsPreferences.mapstyle());
		} // try
		catch(Exception e) {
			// oh dear 
		} // catch
		return TileSourceFactory.getTileSource("CycleStreets");
		
		//return OPENCYCLEMAP;
	} // mapRenderer
	
	static 
	{ 
		final OnlineTileSourceBase OPENCYCLEMAP = new XYTileSource("CycleStreets",
	            			ResourceProxy.string.cyclemap, 0, 17, 256, ".png",
	            			"http://a.tile.opencyclemap.org/cycle/",
	            			"http://b.tile.opencyclemap.org/cycle/",
	            			"http://c.tile.opencyclemap.org/cycle/");
		TileSourceFactory.addTileSource(OPENCYCLEMAP);
	}
	 
} // CycleMapView