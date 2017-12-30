package net.cyclestreets.views;

import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.views.overlay.LocationOverlay;
import net.cyclestreets.views.overlay.ControllerOverlay;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import java.util.List;

public class CycleMapView extends FrameLayout
{
  private static final String PREFS_APP_CENTRE_LON = "centreLon";
  private static final String PREFS_APP_CENTRE_LAT = "centreLat";
  private static final String PREFS_APP_ZOOM_LEVEL = "zoomLevel";
  private static final String PREFS_APP_MY_LOCATION = "myLocation";
  private static final String PREFS_APP_FOLLOW_LOCATION = "followLocation";

  private MapView mapView_;
  private ITileSource renderer_;
  private final SharedPreferences prefs_;
  private final ControllerOverlay controllerOverlay_;
  private final LocationOverlay location_;
  private final int overlayBottomIndex_;

  private IGeoPoint centreOn_ = null;

  public CycleMapView(final Context context, final String name) {
    super(context);

    mapView_ = new MapView(context, TileSource.mapTileProvider(context));
    addView(mapView_);

    prefs_ = context.getSharedPreferences("net.cyclestreets.mapview."+name, Context.MODE_PRIVATE);

    mapView_.setBuiltInZoomControls(false);
    mapView_.setMultiTouchControls(true);

    overlayBottomIndex_ = getOverlays().size();

    location_ = new LocationOverlay(this);
    getOverlays().add(location_);

    controllerOverlay_ = new ControllerOverlay(this);
    getOverlays().add(controllerOverlay_);
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
  public MapView mapView() { return mapView_; }
  public List<Overlay> getOverlays() { return mapView_.getOverlays(); }
  public IGeoPoint getMapCenter() { return mapView_.getMapCenter(); }
  private MapTileProviderBase getTileProvider() { return mapView_.getTileProvider(); }
  public int getZoomLevel() { return mapView_.getZoomLevel(); }
  private Scroller getScroller() { return mapView_.getScroller(); }
  public IMapController getController() { return mapView_.getController(); }
  public BoundingBox getBoundingBox() { return mapView_.getBoundingBox(); }
  public Projection getProjection() { return mapView_.getProjection(); }
  public void zoomToBoundingBox(BoundingBoxE6 boundingBox) { mapView_.zoomToBoundingBox(boundingBox); }

  private void setTileSource(ITileSource tileSource) { mapView_.setTileSource(tileSource); }
  public void setMapListener(MapListener ml) { mapView_.setMapListener(ml); }

  /////////////////////////////////////////
  // save/restore
  public void onPause()
  {
    final SharedPreferences.Editor edit = prefs_.edit();
    final IGeoPoint centre = getMapCenter();
    int lon = centre.getLongitudeE6();
    int lat = centre.getLatitudeE6();
    edit.putInt(PREFS_APP_CENTRE_LON, lon);
    edit.putInt(PREFS_APP_CENTRE_LAT, lat);
    edit.putInt(PREFS_APP_ZOOM_LEVEL, getZoomLevel());
    edit.putBoolean(PREFS_APP_MY_LOCATION, location_.isMyLocationEnabled());
    edit.putBoolean(PREFS_APP_FOLLOW_LOCATION, location_.isFollowLocationEnabled());

    disableMyLocation();

    controllerOverlay_.onPause(edit);
    edit.commit();

    // These lines effectively shut down the map.
    // This object needs to be discarded and re-created on resuming.
    getTileProvider().detach();
    getTileProvider().clearTileCache();
    BitmapPool.getInstance().clearBitmapPool();
  } // onPause

  public void onResume()
  {
    final ITileSource tileSource = mapRenderer();
    if(!tileSource.equals(renderer_)) {
      renderer_ = tileSource;
      setTileSource(renderer_);
    } // if ...

    location_.enableLocation(pref(PREFS_APP_MY_LOCATION, CycleMapDefaults.gps()));
    if(pref(PREFS_APP_FOLLOW_LOCATION, CycleMapDefaults.gps()))
      location_.enableFollowLocation();
    else
      location_.disableFollowLocation();

    if(centreOn_ == null) {
      // mild data race if we're setting centre in onActivityResult
      // because that's followed by an onResume
      GeoPoint defCentre = CycleMapDefaults.centre();
      int lat = pref(PREFS_APP_CENTRE_LAT, defCentre.getLatitudeE6()); /* Greenwich */
      int lon = pref(PREFS_APP_CENTRE_LON, defCentre.getLongitudeE6());
      final GeoPoint centre = new GeoPoint(lat, lon);
      getScroller().abortAnimation();
      getController().setCenter(centre);
      centreOn(centre);
    } // if ...

    getController().setZoom(pref(PREFS_APP_ZOOM_LEVEL, 14));

    controllerOverlay_.onResume(prefs_);
  } // onResume

  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public void onCreateOptionsMenu(final Menu menu)
  {
    controllerOverlay_.onCreateOptionsMenu(menu);
  } // onCreateOptionsMenu

  public void onPrepareOptionsMenu(final Menu menu)
  {
    controllerOverlay_.onPrepareOptionsMenu(menu);
  } // onPrepareOptionsMenu

  public boolean onMenuItemSelected(final int featureId, final MenuItem item)
  {
    return controllerOverlay_.onMenuItemSelected(featureId, item);
  } // onMenuItemSelected

  @Override
  public void onCreateContextMenu(final ContextMenu menu)
  {
    controllerOverlay_.onCreateContextMenu(menu);
  } //  onCreateContextMenu

  public boolean onBackPressed()
  {
    return controllerOverlay_.onBackPressed();
  } // onBackPressed

  /////////////////////////////////////////
  // location
  public Location getLastFix() { return location_.getLastFix(); }
  public boolean isMyLocationEnabled() { return location_.isMyLocationEnabled(); }
  public void toggleMyLocation() { location_.enableLocation(!location_.isMyLocationEnabled()); }
  public void disableMyLocation() { location_.disableMyLocation(); }
  public void disableFollowLocation() { location_.disableFollowLocation(); }

  public void enableAndFollowLocation() { location_.enableAndFollowLocation(true); }
  public void lockOnLocation() { location_.lockOnLocation(); }
  public void hideLocationButton() { location_.hideButton(); }

  ///////////////////////////////////////////////////////
  public void centreOn(final IGeoPoint place)
  {
    centreOn_ = place;
    invalidate();
  } // centreOn

  @Override
  protected void dispatchDraw(final Canvas canvas)
  {
    if(centreOn_  != null)
    {
      getController().animateTo(new GeoPoint(centreOn_.getLatitudeE6(), centreOn_.getLongitudeE6()));
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

  public String mapAttribution() {
    return TileSource.mapAttribution();
  } // mapAttribution

  private ITileSource mapRenderer() {
    return TileSource.mapRenderer(getContext());
  } // mapRenderer
} // CycleMapView
