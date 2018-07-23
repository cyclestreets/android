package net.cyclestreets.views.overlay;

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

public class ItemizedOverlay<Item extends OverlayItem> extends Overlay implements TapListener {
  private final MapView mapView_;
  private final List<Item> items_;

  // utility vars to avoid repeated allocations
  private final Rect rect_ = new Rect();
  private final Point itemPoint_ = new Point();
  private final Point screenCoords = new Point();

  protected MapView mapView() { return mapView_; }

  public ItemizedOverlay(final MapView mapView,
                         final List<Item> items) {
    super();
    mapView_ = mapView;
    items_ = items;
  }

  protected List<Item> items() { return items_; }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    if (shadow)
      return;

    final float scale = mapView.getContext().getResources().getDisplayMetrics().density;
    final float orientation = mapView.getMapOrientation();

    final Projection pj = mapView.getProjection();
    for (int i = items_.size() -1; i >= 0; --i) {
      final Item item = items_.get(i);
      pj.toPixels(item.getPoint(), screenCoords);
      onDrawItem(canvas, item, screenCoords, scale, orientation);
    }
  }

  private void onDrawItem(final Canvas canvas,
                          final Item item,
                          final Point curScreenCoords,
                          final float scale,
                          final float mapOrientation) {
    final HotspotPlace hotspot = item.getMarkerHotspot();
    final Drawable marker = item.getMarker(0);
    boundToHotspot(marker, hotspot, scale);

    int x = curScreenCoords.x;
    int y = curScreenCoords.y;

    Matrix matrix = mapView_.getMatrix();
    float matrixValues[] = new float[9];
    matrix.getValues(matrixValues);

    float scaleX = (float) Math.sqrt(matrixValues[Matrix.MSCALE_X]
        * matrixValues[Matrix.MSCALE_X] + matrixValues[Matrix.MSKEW_Y]
        * matrixValues[Matrix.MSKEW_Y]);
    float scaleY = (float) Math.sqrt(matrixValues[Matrix.MSCALE_Y]
        * matrixValues[Matrix.MSCALE_Y] + matrixValues[Matrix.MSKEW_X]
        * matrixValues[Matrix.MSKEW_X]);

    canvas.save();
    canvas.rotate(-mapOrientation, x, y);
    canvas.scale(1 / scaleX, 1 / scaleY, x, y);

    marker.copyBounds(rect_);
    marker.setBounds(rect_.left + x, rect_.top + y, rect_.right + x, rect_.bottom + y);
    marker.draw(canvas);
    marker.setBounds(rect_);

    canvas.restore();
  }

  private boolean hitTest(final Drawable marker,
                          final int hitX,
                          final int hitY) {
    return marker.getBounds().contains(hitX, hitY);
  }

  private Drawable boundToHotspot(final Drawable marker,
                                  HotspotPlace hotspot,
                                  final float scale) {
    int markerWidth = (int) (marker.getIntrinsicWidth() * scale);
    int markerHeight = (int) (marker.getIntrinsicHeight() * scale);

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
  }

  /////////////////////////////////////////////////
  @Override
  public boolean onSingleTap(final MotionEvent event) {
    return (activateSelectedItems(event, mapView_, new ActiveItem<Item>() {
      @Override
      public boolean run(final Item item) {
        return onItemSingleTap(item);
      }
    }));
  }

  @Override
  public boolean onDoubleTap(final MotionEvent event) {
    return (activateSelectedItems(event, mapView_, new ActiveItem<Item>() {
      @Override
      public boolean run(final Item item) {
        return onItemDoubleTap(item);
      }
    }));
  }

  protected boolean onItemSingleTap(final Item item) {
    return false;
  }

  protected boolean onItemDoubleTap(final Item item) {
    return false;
  }

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
    }
    return false;
  }

  private interface ActiveItem<Item> {
    boolean run(final Item aIndex);
  }
}
