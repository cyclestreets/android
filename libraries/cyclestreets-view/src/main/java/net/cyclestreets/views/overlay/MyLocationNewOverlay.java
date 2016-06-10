package net.cyclestreets.views.overlay;

import java.util.LinkedList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Overlay.Snappable;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

/**
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 *
 */
class MyLocationNewOverlay extends Overlay implements IMyLocationConsumer,
    IOverlayMenuProvider, Snappable {
  private static final Logger logger = LoggerFactory.getLogger(MyLocationNewOverlay.class);

  // ===========================================================
  // Constants
  // ===========================================================

  // ===========================================================
  // Fields
  // ===========================================================

  protected final Paint mPaint = new Paint();
  protected final Paint mCirclePaint = new Paint();

  protected final Bitmap mPersonBitmap;
  protected final Bitmap mDirectionArrowBitmap;

  protected final MapView mMapView;

  private final IMapController mMapController;
  public IMyLocationProvider mMyLocationProvider;

  private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<>();
  private final Point mMapCoords = new Point();

  private Location mLocation;
  private final GeoPoint mGeoPoint = new GeoPoint(0, 0); // for reuse
  private boolean mIsLocationEnabled = false;
  protected boolean mIsFollowing = false; // follow location updates
  protected boolean mDrawAccuracyEnabled = true;

  /** Coordinates the feet of the person are located scaled for display density. */
  protected final PointF mPersonHotspot;

  protected final float mDirectionArrowCenterX;
  protected final float mDirectionArrowCenterY;

  public static final int MENU_MY_LOCATION = getSafeMenuId();

  private boolean mOptionsMenuEnabled = true;

  // to avoid allocations during onDraw
  private final float[] mMatrixValues = new float[9];
  private final Matrix mMatrix = new Matrix();
  private final Rect mMyLocationRect = new Rect();
  private final Rect mMyLocationPreviousRect = new Rect();

  // ===========================================================
  // Constructors
  // ===========================================================

  public MyLocationNewOverlay(Context context, MapView mapView) {
    this(context, new GpsMyLocationProvider(context), mapView);
  }

  public MyLocationNewOverlay(Context context, IMyLocationProvider myLocationProvider,
                              MapView mapView) {
    this(myLocationProvider, mapView, new DefaultResourceProxyImpl(context));
  }

  public MyLocationNewOverlay(IMyLocationProvider myLocationProvider, MapView mapView,
                              ResourceProxy resourceProxy) {
    super(resourceProxy);

    mMapView = mapView;
    mMapController = mapView.getController();
    mCirclePaint.setARGB(0, 100, 100, 255);
    mCirclePaint.setAntiAlias(true);

    mPersonBitmap = mResourceProxy.getBitmap(ResourceProxy.bitmap.person);
    mDirectionArrowBitmap = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);

    mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0f - 0.5f;
    mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0f - 0.5f;

    // Calculate position of person icon's feet, scaled to screen density
    mPersonHotspot = new PointF(24.0f * mScale + 0.5f, 39.0f * mScale + 0.5f);

    setMyLocationProvider(myLocationProvider);
  }

  @Override
  public void onDetach(MapView mapView) {
    this.disableMyLocation();
    super.onDetach(mapView);
  }

  protected void setMyLocationProvider(IMyLocationProvider myLocationProvider) {
    if (myLocationProvider == null)
      throw new RuntimeException(
          "You must pass an IMyLocationProvider to setMyLocationProvider()");

    if (mMyLocationProvider != null)
      mMyLocationProvider.stopLocationProvider();

    mMyLocationProvider = myLocationProvider;
  }

  protected void drawMyLocation(final Canvas canvas,
                                final MapView mapView,
                                final Location lastFix) {
    final Projection pj = mapView.getProjection();

    // mMapCoords are wrong for tileSize != 256
    pj.toPixels(new GeoPoint(lastFix), mMapCoords);
    // final int zoomDiff = MapViewConstants.mMapView.getMaxZoomLevel() - pj.getZoomLevel();
    final float x = mMapCoords.x;
    final float y = mMapCoords.y;

    if (mDrawAccuracyEnabled) {
      final float radius = lastFix.getAccuracy()
          / (float) TileSystem.GroundResolution(lastFix.getLatitude(),
          mapView.getZoomLevel());

      mCirclePaint.setAlpha(50);
      mCirclePaint.setStyle(Style.FILL);
      canvas.drawCircle(x, y, radius, mCirclePaint);

      mCirclePaint.setAlpha(150);
      mCirclePaint.setStyle(Style.STROKE);
      canvas.drawCircle(x, y, radius, mCirclePaint);
    }

    canvas.getMatrix(mMatrix);
    mMatrix.getValues(mMatrixValues);

    // Calculate real scale including accounting for rotation
    float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
        * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
        * mMatrixValues[Matrix.MSKEW_Y]);
    float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
        * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
        * mMatrixValues[Matrix.MSKEW_X]);
    if (lastFix.hasBearing()) {
      canvas.save();
      // Rotate the icon
      canvas.rotate(lastFix.getBearing(), x, y);
      // Counteract any scaling that may be happening so the icon stays the same size
      canvas.scale(1 / scaleX, 1 / scaleY, x, y);
      // Draw the bitmap
      canvas.drawBitmap(mDirectionArrowBitmap, x - mDirectionArrowCenterX, y
          - mDirectionArrowCenterY, mPaint);
      canvas.restore();
    } else {
      canvas.save();
      // Unrotate the icon if the maps are rotated so the little man stays upright
      canvas.rotate(-mMapView.getMapOrientation(), x, y);
      // Counteract any scaling that may be happening so the icon stays the same size
      canvas.scale(1 / scaleX, 1 / scaleY, x, y);
      // Draw the bitmap
      canvas.drawBitmap(mPersonBitmap, x - mPersonHotspot.x, y - mPersonHotspot.y, mPaint);
      canvas.restore();
    }
  }

  protected Rect getMyLocationDrawingBounds(int zoomLevel, Location lastFix, Rect reuse) {
    if (reuse == null)
      reuse = new Rect();

    final int zoomDiff = mMapView.getMaxZoomLevel() - zoomLevel;
    final int posX = mMapCoords.x >> zoomDiff;
    final int posY = mMapCoords.y >> zoomDiff;

    // Start with the bitmap bounds
    if (lastFix.hasBearing()) {
      // Get a square bounding box around the object, and expand by the length of the diagonal
      // so as to allow for extra space for rotating
      int widestEdge = (int) Math.ceil(Math.max(mDirectionArrowBitmap.getWidth(),
          mDirectionArrowBitmap.getHeight()) * Math.sqrt(2));
      reuse.set(posX, posY, posX + widestEdge, posY + widestEdge);
      reuse.offset(-widestEdge / 2, -widestEdge / 2);
    } else {
      reuse.set(posX, posY, posX + mPersonBitmap.getWidth(), posY + mPersonBitmap.getHeight());
      reuse.offset((int) (-mPersonHotspot.x + 0.5f), (int) (-mPersonHotspot.y + 0.5f));
    }

    // Add in the accuracy circle if enabled
    if (mDrawAccuracyEnabled) {
      final int radius = (int)Math.ceil(lastFix.getAccuracy()
          / (float) TileSystem.GroundResolution(lastFix.getLatitude(), zoomLevel));
      reuse.union(posX - radius, posY - radius, posX + radius, posY + radius);
      final int strokeWidth = (int)Math.ceil(mCirclePaint.getStrokeWidth() == 0 ? 1
          : mCirclePaint.getStrokeWidth());
      reuse.inset(-strokeWidth, -strokeWidth);
    }

    return reuse;
  }

  // ===========================================================
  // Methods from SuperClass/Interfaces
  // ===========================================================

  @Override
  protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
    if (shadow)
      return;

    if (mLocation != null && isMyLocationEnabled()) {
      drawMyLocation(canvas, mapView, mLocation);
    }
  }

  @Override
  public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
                              final IMapView mapView) {
    if (this.mLocation != null) {
      snapPoint.x = mMapCoords.x;
      snapPoint.y = mMapCoords.y;
      final double xDiff = x - mMapCoords.x;
      final double yDiff = y - mMapCoords.y;
      final boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
      return snap;
    } else {
      return false;
    }
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
    if (event.getAction() == MotionEvent.ACTION_MOVE) {
      this.disableFollowLocation();
    }

    return super.onTouchEvent(event, mapView);
  }

  // ===========================================================
  // Menu handling methods
  // ===========================================================

  @Override
  public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled) {
    this.mOptionsMenuEnabled = pOptionsMenuEnabled;
  }

  @Override
  public boolean isOptionsMenuEnabled() {
    return this.mOptionsMenuEnabled;
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                     final MapView pMapView) {
    pMenu.add(0, MENU_MY_LOCATION + pMenuIdOffset, Menu.NONE,
        mResourceProxy.getString(ResourceProxy.string.my_location))
        .setIcon(mResourceProxy.getDrawable(ResourceProxy.bitmap.ic_menu_mylocation))
        .setCheckable(true);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                      final MapView pMapView) {
    pMenu.findItem(MENU_MY_LOCATION + pMenuIdOffset).setChecked(this.isMyLocationEnabled());
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
                                       final MapView pMapView) {
    final int menuId = pItem.getItemId() - pMenuIdOffset;
    if (menuId == MENU_MY_LOCATION) {
      if (this.isMyLocationEnabled()) {
        this.disableFollowLocation();
        this.disableMyLocation();
      } else {
        this.enableFollowLocation();
        this.enableMyLocation();
      }
      return true;
    } else {
      return false;
    }
  }

  // ===========================================================
  // Methods
  // ===========================================================

  /**
   * Return a GeoPoint of the last known location, or null if not known.
   */
  public GeoPoint getMyLocation() {
    if (mLocation == null) {
      return null;
    } else {
      return new GeoPoint(mLocation);
    }
  }

  public Location getLastFix() {
    return mLocation;
  }

  /**
   * Enables "follow" functionality. The map will center on your current location and
   * automatically scroll as you move. Scrolling the map in the UI will disable.
   */
  public void enableFollowLocation() {
    mIsFollowing = true;

    // set initial location when enabled
    if (isMyLocationEnabled()) {
      mLocation = mMyLocationProvider.getLastKnownLocation();
      if (mLocation != null) {
        TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
            mMapView.getMaxZoomLevel(), mMapCoords);
        final int worldSize_2 = TileSystem.MapSize(mMapView.getMaxZoomLevel()) / 2;
        mMapCoords.offset(-worldSize_2, -worldSize_2);
        mMapController.animateTo(new GeoPoint(mLocation));
      }
    }

    // Update the screen to see changes take effect
    if (mMapView != null) {
      mMapView.postInvalidate();
    }
  }

  /**
   * Disables "follow" functionality.
   */
  public void disableFollowLocation() {
    mIsFollowing = false;
  }

  /**
   * If enabled, the map will center on your current location and automatically scroll as you
   * move. Scrolling the map in the UI will disable.
   *
   * @return true if enabled, false otherwise
   */
  public boolean isFollowLocationEnabled() {
    return mIsFollowing;
  }

  @Override
  public void onLocationChanged(Location location, IMyLocationProvider source) {
    // If we had a previous location, let's get those bounds
    Location oldLocation = mLocation;
    if (oldLocation != null) {
      this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), oldLocation,
          mMyLocationPreviousRect);
    }

    mLocation = location;
    mMapCoords.set(0, 0);

    if (mLocation != null) {
      TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
          mMapView.getMaxZoomLevel(), mMapCoords);
      final int worldSize_2 = TileSystem.MapSize(mMapView.getMaxZoomLevel()) / 2;
      mMapCoords.offset(-worldSize_2, -worldSize_2);

      if (mIsFollowing) {
        mGeoPoint.setLatitudeE6((int) (mLocation.getLatitude() * 1E6));
        mGeoPoint.setLongitudeE6((int) (mLocation.getLongitude() * 1E6));
        mMapController.animateTo(mGeoPoint);
      } else {
        // Get new drawing bounds
        this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), mLocation, mMyLocationRect);

        // If we had a previous location, merge in those bounds too
        if (oldLocation != null) {
          mMyLocationRect.union(mMyLocationPreviousRect);
        }

        final int left = mMyLocationRect.left;
        final int top = mMyLocationRect.top;
        final int right = mMyLocationRect.right;
        final int bottom = mMyLocationRect.bottom;

        // Invalidate the bounds
        mMapView.post(new Runnable() {
          @Override
          public void run() {
            mMapView.invalidateMapCoordinates(left, top, right, bottom);
          }
        });
      }
    }

    for (final Runnable runnable : mRunOnFirstFix) {
      new Thread(runnable).start();
    }
    mRunOnFirstFix.clear();
  }

  public boolean enableMyLocation(IMyLocationProvider myLocationProvider) {
    this.setMyLocationProvider(myLocationProvider);
    mIsLocationEnabled = false;
    return enableMyLocation();
  }

  /**
   * Enable receiving location updates from the provided IMyLocationProvider and show your
   * location on the maps. You will likely want to call enableMyLocation() from your Activity's
   * Activity.onResume() method, to enable the features of this overlay. Remember to call the
   * corresponding disableMyLocation() in your Activity's Activity.onPause() method to turn off
   * updates when in the background.
   */
  public boolean enableMyLocation() {
    if (mIsLocationEnabled)
      mMyLocationProvider.stopLocationProvider();

    boolean result = mMyLocationProvider.startLocationProvider(this);
    mIsLocationEnabled = result;

    // set initial location when enabled
    if (result && isFollowLocationEnabled()) {
      mLocation = mMyLocationProvider.getLastKnownLocation();
      if (mLocation != null) {
        TileSystem.LatLongToPixelXY(mLocation.getLatitude(), mLocation.getLongitude(),
            mMapView.getMaxZoomLevel(), mMapCoords);
        final int worldSize_2 = TileSystem.MapSize(mMapView.getMaxZoomLevel()) / 2;
        mMapCoords.offset(-worldSize_2, -worldSize_2);
        mMapController.animateTo(new GeoPoint(mLocation));
      }
    }

    // Update the screen to see changes take effect
    if (mMapView != null) {
      mMapView.postInvalidate();
    }

    return result;
  }

  /**
   * Disable location updates
   */
  public void disableMyLocation() {
    mIsLocationEnabled = false;

    if (mMyLocationProvider != null) {
      mMyLocationProvider.stopLocationProvider();
    }

    // Update the screen to see changes take effect
    if (mMapView != null) {
      mMapView.postInvalidate();
    }
  }

  /**
   * If enabled, the map is receiving location updates and drawing your location on the map.
   *
   * @return true if enabled, false otherwise
   */
  public boolean isMyLocationEnabled() {
    return mIsLocationEnabled;
  }
}
