package net.cyclestreets.views;

import net.cyclestreets.views.overlay.LocationOverlay;
import net.cyclestreets.views.overlay.TapOverlay;
import net.cyclestreets.views.overlay.ZoomButtonsOverlay;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import uk.org.invisibility.cycloid.CycloidConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

public class CycleMapView extends MapView
{
	private final SharedPreferences prefs_;
	private final LocationOverlay location_;
	private final int overlayBottomIndex_;
	
	public CycleMapView(final Context context, final String name)
	{
		super(context, null);

		prefs_ = context.getSharedPreferences(CycloidConstants.PREFS_APP_KEY+"."+name, Context.MODE_PRIVATE);
		
		setTileSource(mapRenderer());
        setBuiltInZoomControls(false);
        setMultiTouchControls(true);
        getController().setZoom(prefs_.getInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, 14));
        scrollTo(prefs_.getInt(CycloidConstants.PREFS_APP_SCROLL_X, 0), 
        		 prefs_.getInt(CycloidConstants.PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */
	
        overlayBottomIndex_ = getOverlays().size();
        
        getOverlays().add(new ZoomButtonsOverlay(context, this));

        location_ = new LocationOverlay(context, this);
        getOverlays().add(location_);

        getOverlays().add(new TapOverlay(context, this));
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
        edit.putBoolean(CycloidConstants.PREFS_APP_FOLLOW_LOCATION, location_.isLocationFollowEnabled());
        edit.commit();

        disableMyLocation();
	} // onPause

	public void onResume()
	{
        location_.enableLocation(prefs_.getBoolean(CycloidConstants.PREFS_APP_MY_LOCATION, false));
        location_.followLocation(prefs_.getBoolean(CycloidConstants.PREFS_APP_FOLLOW_LOCATION, false));
        
        getScroller().abortAnimation();
        
        scrollTo(prefs_.getInt(CycloidConstants.PREFS_APP_SCROLL_X, 0), prefs_.getInt(CycloidConstants.PREFS_APP_SCROLL_Y, -701896)); /* Greenwich */
        getController().setZoom(prefs_.getInt(CycloidConstants.PREFS_APP_ZOOM_LEVEL, 14));
	} // onResume 
	
	/////////////////////////////////////////
	// location
	public Location getLastFix() { return location_.getLastFix(); }
	public void toggleMyLocation() { location_.enableLocation(!location_.isMyLocationEnabled()); }
	public void disableMyLocation() { location_.disableMyLocation(); }
	public void disableFollowLocation() { location_.followLocation(false); }
	
	public void centreOn(final GeoPoint centre)
	{
		location_.centreOn(centre);
		invalidate();
	} // centreOn
		
	private ITileSource mapRenderer()
	{
		return TileSourceFactory.getTileSource(prefs_.getString(CycloidConstants.PREFS_APP_RENDERER, CycloidConstants.DEFAULT_MAPTYPE));
	} // mapRenderer
} // CycleMapView