package net.cyclestreets.views.overlay;

import net.cyclestreets.view.R;

import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class ZoomButtonsOverlay extends ButtonOnlyOverlay {
  private final MapView mapView_;
  private final OverlayButton zoomIn_;
  private final OverlayButton zoomOut_;

  public ZoomButtonsOverlay(final MapView mapView) {
    super();

    mapView_ = mapView;

    final Context context = mapView_.getContext();
    final int offset = DrawingHelper.offset(context);
    final float radius = DrawingHelper.cornerRadius(context);

    final Resources res = context.getResources();
    zoomIn_ = new OverlayButton(res.getDrawable(R.drawable.btn_plus),
                                offset,
                                offset*2,
                                radius);
    zoomIn_.rightAlign().bottomAlign();

    zoomOut_ = new OverlayButton(res.getDrawable(R.drawable.btn_minus),
                                 zoomIn_.right() + offset,
                                 offset*2,
                                 radius);
    zoomOut_.rightAlign().bottomAlign();
  }

  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView) {
    zoomIn_.enable(mapView.canZoomIn());
    zoomIn_.draw(canvas);
    zoomOut_.enable(mapView.canZoomOut());
    zoomOut_.draw(canvas);
  }

  //////////////////////////////////////////////
  @Override
  public boolean onButtonTap(final MotionEvent event) {
    return tapZoom(event);
  }

  @Override
  public boolean onButtonDoubleTap(final MotionEvent event) {
    return zoomIn_.hit(event) || zoomOut_.hit(event);
  }

  private boolean tapZoom(final MotionEvent event) {
    if (zoomIn_.hit(event)) {
      if (zoomIn_.enabled())
        mapView_.getController().zoomIn();
      return true;
    }
    if (zoomOut_.hit(event)) {
      if (zoomOut_.enabled())
        mapView_.getController().zoomOut();
      return true;
    }

    return false;
  }
}
