package net.cyclestreets.views.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import java.util.List;

public class ItemizedOverlay<Item extends OverlayItem> extends Overlay
                                                       implements TapListener {
  private final MapView mapView_;
  private final List<Item> items_;

  // utility vars to avoid repeated allocations
  private final Rect rect_ = new Rect();
  private final Matrix matrix_ = new Matrix();
  private float matrixValues_[] = new float[9];
  private final Point itemPoint_ = new Point();
  private final Point screenCoords = new Point();

  protected MapView mapView() { return mapView_; }
  
  public ItemizedOverlay(final Context context,
                         final MapView mapView, 
                         final List<Item> items) {
    super(context);
    mapView_ = mapView;
    items_ = items;
  }

  protected List<Item> items() { return items_; }
  
  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    if(shadow)
      return;
    
    final Projection pj = mapView.getProjection();
    for (int i = items_.size() -1; i >= 0; --i) {
      final Item item = items_.get(i);
      pj.toPixels(item.getPoint(), screenCoords);
      onDrawItem(canvas, item, screenCoords, mapView.getMapOrientation());
    } // for ...
  } // draw

  private void onDrawItem(final Canvas canvas,
                          final Item item,
                          final Point curScreenCoords,
                          final float mapOrientation) {
    final HotspotPlace hotspot = item.getMarkerHotspot();
    final Drawable marker = item.getMarker(0);
    boundToHotspot(marker, hotspot);

    int x = curScreenCoords.x;
    int y = curScreenCoords.y;

    canvas.getMatrix(matrix_);
    matrix_.getValues(matrixValues_);

    float scaleX = (float) Math.sqrt(matrixValues_[Matrix.MSCALE_X]
        * matrixValues_[Matrix.MSCALE_X] + matrixValues_[Matrix.MSKEW_Y]
        * matrixValues_[Matrix.MSKEW_Y]);
    float scaleY = (float) Math.sqrt(matrixValues_[Matrix.MSCALE_Y]
        * matrixValues_[Matrix.MSCALE_Y] + matrixValues_[Matrix.MSKEW_X]
        * matrixValues_[Matrix.MSKEW_X]);

    canvas.save();
    canvas.rotate(-mapOrientation, x, y);
    canvas.scale(1 / scaleX, 1 / scaleY, x, y);

    marker.copyBounds(rect_);
    marker.setBounds(rect_.left + x, rect_.top + y, rect_.right + x, rect_.bottom + y);
    marker.draw(canvas);
    marker.setBounds(rect_);

    canvas.restore();
  } // onDrawItem

  private boolean hitTest(final Drawable marker,
                          final int hitX,
                          final int hitY) {
    return marker.getBounds().contains(hitX, hitY);
  } // hitTest

  private Drawable boundToHotspot(Drawable marker, HotspotPlace hotspot) {
    int markerWidth = (int) (marker.getIntrinsicWidth() * mScale);
    int markerHeight = (int) (marker.getIntrinsicHeight() * mScale);

    rect_.set(0, 0, markerWidth, markerHeight);

    if (hotspot == null)
      hotspot = HotspotPlace.BOTTOM_CENTER;

    switch (hotspot) {
      default:
      case NONE:
        break;
      case CENTER:
        rect_.offset(-markerWidth / 2, -markerHeight / 2);
        break;
      case BOTTOM_CENTER:
        rect_.offset(-markerWidth / 2, -markerHeight);
        break;
      case TOP_CENTER:
        rect_.offset(-markerWidth / 2, 0);
        break;
      case RIGHT_CENTER:
        rect_.offset(-markerWidth, -markerHeight / 2);
        break;
      case LEFT_CENTER:
        rect_.offset(0, -markerHeight / 2);
        break;
      case UPPER_RIGHT_CORNER:
        rect_.offset(-markerWidth, 0);
        break;
      case LOWER_RIGHT_CORNER:
        rect_.offset(-markerWidth, -markerHeight);
        break;
      case UPPER_LEFT_CORNER:
        rect_.offset(0, 0);
        break;
      case LOWER_LEFT_CORNER:
        rect_.offset(0, markerHeight);
        break;
    }
    marker.setBounds(rect_);
    return marker;
  } // boundToHotSpot
  
  /////////////////////////////////////////////////
  @Override
  public boolean onSingleTap(final MotionEvent event) {
    return (activateSelectedItems(event, mapView_, new ActiveItem<Item>() {
      @Override
      public boolean run(final Item item) {
        return onItemSingleTap(item);
      }
    }));
  } // onSingleTap

  @Override
  public boolean onDoubleTap(final MotionEvent event) {
    return (activateSelectedItems(event, mapView_, new ActiveItem<Item>() {
      @Override
      public boolean run(final Item item) {
        return onItemDoubleTap(item);
      }
    }));
  } // onDoubleTap

  protected boolean onItemSingleTap(final Item item) {
    return false;
  } // onItemSingleTap

  protected boolean onItemDoubleTap(final Item item) {
    return false;
  } // onItemDoubleTap

  /////////////////////////////////////
  private boolean activateSelectedItems(final MotionEvent event,
                                        final MapView mapView,
                                        final ActiveItem<Item> task) {
    final Projection pj = mapView.getProjection();
    final Rect screenRect = pj.getIntrinsicScreenRect();
    final int eventX = screenRect.left + (int)event.getX();
    final int eventY = screenRect.top + (int)event.getY();

    for (final Item item : items_) {
      pj.toPixels(item.getPoint(), itemPoint_);

      final Drawable marker = item.getMarker(0);
      if (hitTest(marker,
                  eventX - itemPoint_.x,
                  eventY - itemPoint_.y)) {
        if (task.run(item))
          return true;
      }
    } // for ...
    return false;
  } // activateSelectedItems

  private interface ActiveItem<Item> {
    boolean run(final Item aIndex);
  } // interface ActiveItem
} // class ItemizedOverlay
