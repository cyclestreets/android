package net.cyclestreets.views.overlay;

import net.cyclestreets.util.Theme;
import net.cyclestreets.view.R;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.enableMenuItem;

public class LocationOverlay extends MyLocationNewOverlay {
  private final FloatingActionButton button_;

  private final CycleMapView mapView_;
  private final int onColor_;
  private final int followColor_;

  private boolean lockedOn_;

  private static class UseEverythingLocationProvider extends GpsMyLocationProvider {
    public UseEverythingLocationProvider(Context context) {
      super(context);
      LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
      for (String source : locMan.getProviders(true))
        addLocationSource(source);
    }
  }

  public LocationOverlay(final CycleMapView mapView) {
    super(new UseEverythingLocationProvider(mapView.getContext()), mapView.mapView());
    //setLocationUpdateMinTime(500);
    //setLocationUpdateMinDistance(10);

    mapView_ = mapView;

    onColor_ = Theme.lowlightColor(mapView_.getContext());
    followColor_ = Theme.highlightColor(mapView_.getContext()) | 0xFF000000;

    View overlayView = LayoutInflater.from(mapView_.getContext()).inflate(R.layout.locationbutton, null);
    button_ = overlayView.findViewById(R.id.locationbutton);
    button_.setOnClickListener(view -> enableAndFollowLocation(!isFollowLocationEnabled()));

    mapView_.addView(overlayView);

    lockedOn_ = false;
  }

  public void enableLocation(final boolean enable) {
    if (enable)
      enableMyLocation();
    else
      disableMyLocation();
  }

  public void enableAndFollowLocation(final boolean enable) {
    if (enable) {
      try {
        enableMyLocation();
        enableFollowLocation();
        final Location lastFix = getLastFix();
        if (lastFix != null)
          mapView_.getController().setCenter(new GeoPoint(lastFix));
      } catch (RuntimeException e) {
        // might not have location service
      }
    } else {
      disableFollowLocation();
      disableMyLocation();
    }

    mapView_.invalidate();
  }

  public void lockOnLocation() {
    lockedOn_ = true;
  }

  public void hideButton() {
    button_.setVisibility(View.INVISIBLE);
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
    final boolean handled = super.onTouchEvent(event, mapView);

    if (lockedOn_ && isMyLocationEnabled() && (event.getAction() == MotionEvent.ACTION_MOVE))
      enableFollowLocation();

    return handled;
  }

  ////////////////////////////////////////////
  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    if (isFollowLocationEnabled())
      button_.setColorFilter(followColor_);
    else
      button_.setColorFilter(isMyLocationEnabled() ? onColor_ : Color.TRANSPARENT);

    // I'm not thrilled about this but there isn't any other way (short of killing
    // and recreating the overlay) of turning off the little here-you-are man
    if (!isMyLocationEnabled())
      return;

    super.draw(canvas, mapView, shadow);
  }
}
