package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import net.cyclestreets.util.Brush;
import net.cyclestreets.views.CycleMapView;
import static net.cyclestreets.views.CycleMapView.ITEM_ZOOM_LEVEL;

public abstract class LiveItemOverlay<T extends OverlayItem>
          extends ItemizedOverlay<T>
          implements MapListener
{
  private final CycleMapView mapView_;
  private int zoomLevel_;
  private boolean loading_;

  private final int offset_;
  private final float radius_;
  private final Paint textBrush_;
  private final Paint urlBrush_;
  private final boolean showLoading_;

  private static final String LOADING = "Loading ...";

  public LiveItemOverlay(final CycleMapView mapView,
                         final boolean showLoading) {
    super(mapView.mapView(),
          new ArrayList<T>());

    mapView_ = mapView;
    zoomLevel_ = mapView_.getZoomLevel();
    loading_ = false;
    showLoading_ = showLoading;

    final Context context = mapView_.getContext();
    offset_ = DrawingHelperKt.offset(context);
    radius_ = DrawingHelperKt.cornerRadius();
    textBrush_ = Brush.createTextBrush(offset_);
    urlBrush_ = Brush.createUrlBrush(offset_);

    mapView_.setMapListener(new DelayedMapListener(this));
  }

  protected Paint textBrush() { return textBrush_; }
  protected Paint urlBrush() { return urlBrush_; }
  protected int offset() { return offset_; }
  protected float cornerRadius() { return radius_; }

  protected void centreOn(IGeoPoint geoPoint) {
    mapView_.centreOn(geoPoint, ITEM_ZOOM_LEVEL, false);
  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    super.draw(canvas, mapView, shadow);

    if ((!loading_) || (!showLoading_))
      return;

    final Rect bounds = new Rect();
    textBrush().getTextBounds(LOADING, 0, LOADING.length(), bounds);

    int width = bounds.width() + (offset() * 2);
    final Rect screen = canvas.getClipBounds();
    screen.left = screen.centerX() - (width/2);
    screen.top += offset()* 2;
    screen.right = screen.left + width;
    screen.bottom = screen.top + bounds.height() + (offset() * 2);

    final Matrix unscaled = mapView.getProjection().getInvertedScaleRotateCanvasMatrix();
    canvas.save();
    canvas.concat(unscaled);

    DrawingHelperKt.drawRoundRect(canvas, screen, cornerRadius(), Brush.Grey);
    canvas.drawText(LOADING, screen.centerX(), screen.centerY() + bounds.bottom, textBrush());

    canvas.restore();
  }

  @Override
  public boolean onScroll(final ScrollEvent event) {
    refreshItems();
    return true;
  }

  @Override
  public boolean onZoom(final ZoomEvent event) {
    if (event.getZoomLevel() < zoomLevel_)
      items().clear();
    zoomLevel_ = (int)event.getZoomLevel();
    refreshItems();
    return true;
  }

  protected void redraw() {
    mapView_.postInvalidate();
  }

  protected void refreshItems() {
    final IGeoPoint centre = mapView_.getMapCenter();
    final int zoom = mapView_.getZoomLevel();
    final BoundingBox bounds = mapView_.getBoundingBox();

    if (!fetchItemsInBackground(centre, zoom, bounds))
      return;

    loading_ = true;
    redraw();
  }

  protected abstract boolean fetchItemsInBackground(final IGeoPoint mapCentre,
                                                    final int zoom,
                                                    final BoundingBox boundingBox);

  protected void setItems(final List<T> items) {
    for (final T item : items)
      if (!items().contains(item))
        items().add(item);
    if (items().size() > 500)  // arbitrary figure
      items().remove(items().subList(0, 100));
    loading_ = false;
    redraw();
  }
}
