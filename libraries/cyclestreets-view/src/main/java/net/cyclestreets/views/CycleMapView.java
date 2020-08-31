package net.cyclestreets.views;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.location.Location;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Scroller;

import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.util.Logging;
import net.cyclestreets.util.PermissionsKt;
import net.cyclestreets.views.overlay.ControllerOverlay;
import net.cyclestreets.views.overlay.FindPlaceOverlay;
import net.cyclestreets.views.overlay.LocationOverlay;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.List;

import androidx.preference.PreferenceManager;

import static java.lang.Math.abs;
import static net.cyclestreets.util.PermissionsKt.hasPermission;

public class CycleMapView extends FrameLayout
{
  public static final int DEFAULT_ZOOM_LEVEL = 14;
  public static final int FINDPLACE_ZOOM_LEVEL = 16;
  public static final int ITEM_ZOOM_LEVEL = 16;
  public static final int MAX_ZOOM_LEVEL = 19;
  public static final double MIN_ZOOM_LEVEL = 2;
  private static final String TAG = Logging.getTag(CycleMapView.class);

  private static final String PREFS_APP_CENTRE_LON = "centreLon";
  private static final String PREFS_APP_CENTRE_LAT = "centreLat";
  private static final String PREFS_APP_ZOOM_LEVEL = "zoomLevel";
  private static final String PREFS_APP_MY_LOCATION = "myLocation";
  private static final String PREFS_APP_FOLLOW_LOCATION = "followLocation";
  private static final String PREFS_FIND_PLACE_LON = "findPlaceLon";
  private static final String PREFS_FIND_PLACE_LAT = "findPlaceLat";

  private MapView mapView_;
  private ITileSource renderer_;
  private final SharedPreferences prefs_;
  private final ControllerOverlay controllerOverlay_;
  private final LocationOverlay location_;
  private final FindPlaceOverlay findPlaceOverlay_;
  private final int overlayBottomIndex_;

  private IGeoPoint centreOn_ = null;
  private IGeoPoint foundPlace;
  private boolean paused_ = false;

  public CycleMapView(final Context context, final String name) {
    super(context);

    // Make sure we can save map tiles, regardless of whether we have the write-external permission granted.
    boolean hasWritePermission = PermissionsKt.hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
    Log.i(TAG, "Creating map view. App has write-external permission? " + hasWritePermission +
        "; osmdroid base path: " + Configuration.getInstance().getOsmdroidBasePath().getAbsolutePath() +
        "; osmdroid tile cache location: " + Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath());

    mapView_ = new MapView(context, TileSource.mapTileProvider(context));
    addView(mapView_);

    prefs_ = context.getSharedPreferences("net.cyclestreets.mapview."+name, Context.MODE_PRIVATE);

    mapView_.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
    mapView_.setMultiTouchControls(true);
    mapView_.setMaxZoomLevel((double)MAX_ZOOM_LEVEL);
    mapView_.setMinZoomLevel(MIN_ZOOM_LEVEL);
    mapView_.setScrollableAreaLimitLatitude(80, -80, 0);

    overlayBottomIndex_ = getOverlays().size();

    location_ = new LocationOverlay(this);
    getOverlays().add(location_);

    controllerOverlay_ = new ControllerOverlay(this);
    getOverlays().add(controllerOverlay_);

    findPlaceOverlay_ = new FindPlaceOverlay(getContext(), this);
    overlayPushBottom(findPlaceOverlay_);
  }

  public Overlay overlayPushBottom(final Overlay overlay) {
    getOverlays().add(overlayBottomIndex_, overlay);
    return overlay;
  }

  public Overlay overlayPushTop(final Overlay overlay) {
    // Puts overlay at second-to-top, always below ControllerOverlay which remains on top
    int front = getOverlays().size()-1;
    getOverlays().add(front, overlay);
    return overlay;
  }

  /////////////////////////////////////////
  public MapView mapView() { return mapView_; }
  public List<Overlay> getOverlays() { return mapView_.getOverlays(); }
  public IGeoPoint getMapCenter() { return mapView_.getMapCenter(); }
  private MapTileProviderBase getTileProvider() { return mapView_.getTileProvider(); }
  public int getZoomLevel() { return (int)mapView_.getZoomLevelDouble(); }
  private Scroller getScroller() { return mapView_.getScroller(); }
  public IMapController getController() { return mapView_.getController(); }
  public BoundingBox getBoundingBox() { return mapView_.getBoundingBox(); }
  public Projection getProjection() { return mapView_.getProjection(); }
  public void zoomToBoundingBox(BoundingBox boundingBox) { mapView_.zoomToBoundingBox(boundingBox, true); }

  private void setTileSource(ITileSource tileSource) { mapView_.setTileSource(tileSource); }
  public void setMapListener(MapListener ml) { mapView_.addMapListener(ml); }

  /////////////////////////////////////////
  // save/restore
  public void onPause() {
    if (paused_)
      return;

    // Some process seems to somehow set the co-ordinates to (85.051128, -180.0) - the north pole!
    // then pause/resume. Despite extensive debugging, I've not been able to figure out the cause.
    // As a workaround, ignore ridiculous (ant)arctic co-ordinates - I can't imagine anyone needing
    // cycle navigation in such places!
    if (abs(getMapCenter().getLatitude()) > 80) {
      Log.d(TAG, "Working around weird location setting bug");
      return;
    }

    paused_ = true;

    final SharedPreferences.Editor edit = prefs_.edit();
    final IGeoPoint centre = getMapCenter();

    // RouteMapFragment is destroyed by FragmentManager during
    // MainNavDrawerActivity.onResume, so values need to be saved:
    if (foundPlace == null) {
      prefs_.edit().remove(PREFS_FIND_PLACE_LON).apply();
      prefs_.edit().remove(PREFS_FIND_PLACE_LAT).apply();
    }
    else {
      int fpLon = (int) (foundPlace.getLongitude() * 1E6);
      int fpLat = (int) (foundPlace.getLatitude() * 1E6);
      edit.putInt(PREFS_FIND_PLACE_LON, fpLon);
      edit.putInt(PREFS_FIND_PLACE_LAT, fpLat);
    }

    int lon = (int)(centre.getLongitude() * 1E6);
    int lat = (int)(centre.getLatitude() * 1E6);

    edit.putInt(PREFS_APP_CENTRE_LON, lon);
    edit.putInt(PREFS_APP_CENTRE_LAT, lat);
    edit.putInt(PREFS_APP_ZOOM_LEVEL, getZoomLevel());
    edit.putBoolean(PREFS_APP_MY_LOCATION, location_.isMyLocationEnabled());
    edit.putBoolean(PREFS_APP_FOLLOW_LOCATION, location_.isFollowLocationEnabled());
    Log.d(TAG, "onPause: Saving lat/lon=" + lat + "/" + lon + ", zoom=" + getZoomLevel());

    disableMyLocation();

    controllerOverlay_.onPause(edit);
    edit.commit();

    // These lines effectively shut down the map.
    // This object needs to be discarded and re-created on resuming.
    getTileProvider().detach();
    getTileProvider().clearTileCache();
    BitmapPool.getInstance().clearBitmapPool();
  }

  public void onResume() {
    final ITileSource tileSource = mapRenderer();
    if (!tileSource.equals(renderer_)) {
      renderer_ = tileSource;
      setTileSource(renderer_);
    }

    boolean locationEnabled = pref(PREFS_APP_MY_LOCATION, CycleMapDefaults.gps());
    boolean locationFollow = pref(PREFS_APP_FOLLOW_LOCATION, CycleMapDefaults.gps());
    location_.disableFollowLocation();
    location_.enableLocation(locationEnabled);
    if (locationFollow && hasPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION))
      location_.enableAndFollowLocation(true);

    if (prefs_.contains(PREFS_FIND_PLACE_LAT) && prefs_.contains(PREFS_FIND_PLACE_LON)) {
      int fpLat = pref(PREFS_FIND_PLACE_LAT, 999);  // Default value shouldn't ever be needed
      int fpLon = pref(PREFS_FIND_PLACE_LON, 999);  // Default value shouldn't ever be needed
      foundPlace = new GeoPoint(fpLat / 1e6, fpLon / 1e6);
    }
    else {
      foundPlace = null;
    }

    GeoPoint defCentre = CycleMapDefaults.centre();
    int lat = pref(PREFS_APP_CENTRE_LAT, (int)(defCentre.getLatitude() * 1E6));
    int lon = pref(PREFS_APP_CENTRE_LON, (int)(defCentre.getLongitude() * 1E6));
    int zoom = pref(PREFS_APP_ZOOM_LEVEL, DEFAULT_ZOOM_LEVEL);
    Log.d(TAG, "onResume: Loading lat/lon=" + lat + "/" + lon + ", zoom=" + zoom);
    final GeoPoint centre = new GeoPoint(lat / 1e6, lon / 1e6);
    getScroller().abortAnimation();
    getController().setCenter(centre);
    centreOn(centre);

    controllerOverlay_.onResume(prefs_);
    getController().setZoom((double)zoom);

    new CountDownTimer(250, 50) {
      public void onTick(long unfinished) { }
      public void onFinish() { postInvalidate(); }
    }.start();
  }

  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public void onCreateOptionsMenu(final Menu menu) {
    controllerOverlay_.onCreateOptionsMenu(menu);
  }

  public void onPrepareOptionsMenu(final Menu menu) {
    controllerOverlay_.onPrepareOptionsMenu(menu);
  }

  public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
    return controllerOverlay_.onMenuItemSelected(featureId, item);
  }

  @Override
  public void onCreateContextMenu(final ContextMenu menu) {
    controllerOverlay_.onCreateContextMenu(menu);
  }

  public boolean onBackPressed() {
    return controllerOverlay_.onBackPressed();
  }

  /////////////////////////////////////////
  // location
  public Location getLastFix() { return location_.getLastFix(); }
  public boolean isMyLocationEnabled() { return location_.isMyLocationEnabled(); }
  public void disableMyLocation() { location_.disableMyLocation(); }
  public void disableFollowLocation() { location_.disableFollowLocation(); }

  public void enableAndFollowLocation() { location_.enableAndFollowLocation(true); }
  public void lockOnLocation() { location_.lockOnLocation(); }
  public void hideLocationButton() { location_.hideButton(); }
  public void shiftAttribution() { controllerOverlay_.setAttributionShifted(); }

  ///////////////////////////////////////////////////////
  public void centreOn(final IGeoPoint place) {
    centreOn_ = place;
    postInvalidate();
  }

  public void centreOn(final IGeoPoint place, final int minZoomLevel, final boolean foundPlaceTrue) {
    centreOn(place);
    if (this.getZoomLevel() < minZoomLevel)
      getController().setZoom((double)minZoomLevel);
    foundPlace = null;
    if (foundPlaceTrue) {
      foundPlace = place;
      location_.disableFollowLocation();
    }
  }

  public IGeoPoint getFoundPlace() {return foundPlace;}

  public void setFoundPlace(IGeoPoint foundPlaceVal) {foundPlace = foundPlaceVal;}

  @Override
  protected void dispatchDraw(final Canvas canvas) {
    if (centreOn_ != null) {
      Log.d(TAG, "Animating to " + centreOn_.getLatitude() + "/" + centreOn_.getLongitude());
      getController().animateTo(new GeoPoint(centreOn_.getLatitude(), centreOn_.getLongitude()));
      centreOn_ = null;
      return;
    }

    super.dispatchDraw(canvas);
  }

  @Override
  public void invalidate() {
    mapView_.invalidate();
    super.invalidate();
  }

  ///////////////////////////////////////////////////////
  private int pref(final String key, int defValue) {
    return prefs_.getInt(key, defValue);
  }
  private boolean pref(final String key, boolean defValue) {
    return prefs_.getBoolean(key, defValue);
  }

  public String mapAttribution() {
    return TileSource.mapAttribution();
  }

  private ITileSource mapRenderer() {
    return TileSource.mapRenderer(getContext());
  }
}
