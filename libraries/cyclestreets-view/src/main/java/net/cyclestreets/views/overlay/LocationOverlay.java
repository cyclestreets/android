package net.cyclestreets.views.overlay;

import net.cyclestreets.view.R;

import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.enableMenuItem;

public class LocationOverlay extends MyLocationNewOverlay
                             implements ButtonTapListener, MenuListener {
  private final int offset_;
  private final float radius_;

  private final OverlayButton locationButton_;

  private final MapView mapView_;

  private boolean hidden_;
  private boolean lockedOn_;

  public LocationOverlay(final Context context, final MapView mapView) {
    super(mapView);

    //setLocationUpdateMinTime(500);
    //setLocationUpdateMinDistance(10);

    mapView_ = mapView;

    offset_ = DrawingHelper.offset(context);
    radius_ = DrawingHelper.cornerRadius(context);

    final Resources res = context.getResources();
    locationButton_ = new OverlayButton(res.getDrawable(R.drawable.ic_menu_followlocation),
                                        res.getDrawable(R.drawable.ic_menu_mylocation),
                                        offset_,
                                        offset_*2,
                                        radius_);
    locationButton_.bottomAlign();
    locationButton_.rightAlign();

    lockedOn_ = false;
    hidden_ = false;
  } // LocationOverlay

  public void enableLocation(final boolean enable) {
    if(enable)
      enableMyLocation();
    else
      disableMyLocation();
  } // enableLocation

  public void enableAndFollowLocation(final boolean enable) {
    if(enable) {
      try {
        enableMyLocation();
        enableFollowLocation();
        final Location lastFix = getLastFix();
        if (lastFix != null)
          mapView_.getController().setCenter(new GeoPoint(lastFix));
      } catch(RuntimeException e) {
        // might not have location service
      } // catch
    } else {
      disableFollowLocation();
      disableMyLocation();
    } // if ...

    mapView_.invalidate();
  } // enableAndFollowLocation

  public void lockOnLocation() {
    lockedOn_ = true;
  } // lockOnLocation

  public void hideButton() {
    hidden_ = true;
  } // hideButton

  @Override
  public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
    final boolean handled = super.onTouchEvent(event, mapView);

    if(lockedOn_ && isMyLocationEnabled() && (event.getAction() == MotionEvent.ACTION_MOVE))
      enableFollowLocation();

    return handled;
  } // onTouchEvent

  ////////////////////////////////////////////
  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    // I'm not thrilled about this but there isn't any other way (short of killing
    // and recreating the overlay) of turning off the little here-you-are man
    if(!isMyLocationEnabled())
      return;

    super.draw(canvas, mapView, shadow);
  } // onDraw

  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView) {
    if(hidden_)
      return;
    locationButton_.pressed(isFollowLocationEnabled());
    locationButton_.alternate(isMyLocationEnabled());

    locationButton_.draw(canvas);
  } // drawLocationButton

  ////////////////////////////////////////////////
  @Override
  public void onCreateOptionsMenu(final Menu menu) {
    createMenuItem(menu, R.string.location_menu_mylocation, Menu.NONE, R.drawable.ic_menu_mylocation);
  } // onCreateOptionsMenu

  @Override
  public void onPrepareOptionsMenu(final Menu menu) {
    final MenuItem item = enableMenuItem(menu, R.string.location_menu_mylocation, true);
    if(item != null)
      item.setTitle(isMyLocationEnabled() ? R.string.location_menu_off : R.string.location_menu_on);
  } // onPrepareOptionsMenu

  @Override
  public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
    if(item.getItemId() != R.string.location_menu_mylocation)
      return false;

    enableAndFollowLocation(!isMyLocationEnabled());

    return true;
  } // onMenuItemSelected

  //////////////////////////////////////////////
  @Override
  public boolean onButtonTap(final MotionEvent event) {
     if(hidden_)
        return false;
    return tapLocation(event);
  } // onSingleTapUp

  @Override
  public boolean onButtonDoubleTap(final MotionEvent event) {
    if(hidden_)
      return false;
    return locationButton_.hit(event);
  } // onDoubleTap

  private boolean tapLocation(final MotionEvent event) {
    if(!locationButton_.hit(event))
      return false;

    enableAndFollowLocation(!locationButton_.pressed());

    return true;
  } // tapLocation
} // LocationOverlay
