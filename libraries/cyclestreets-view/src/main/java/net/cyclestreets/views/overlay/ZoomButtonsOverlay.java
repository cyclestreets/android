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

  public ZoomButtonsOverlay(final Context context,
                            final MapView mapView) {
    super(context);

    mapView_ = mapView;

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
  } // ZoomButtonsOverlay

  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView) {
    zoomIn_.enable(mapView.canZoomIn());
    zoomIn_.draw(canvas);
    zoomOut_.enable(mapView.canZoomOut());
    zoomOut_.draw(canvas);
  } // drawButtons

  //////////////////////////////////////////////
  @Override
  public boolean onButtonTap(final MotionEvent event) {
    return tapZoom(event);
  } // onSingleTapUp

  @Override
  public boolean onButtonDoubleTap(final MotionEvent event) {
    return zoomIn_.hit(event) || zoomOut_.hit(event);
  } // onDoubleTap

  private boolean tapZoom(final MotionEvent event) {
    if(zoomIn_.hit(event)) {
      if(zoomIn_.enabled())
        mapView_.getController().zoomIn();
      return true;
    } // if ...
    if(zoomOut_.hit(event)) {
      if(zoomOut_.enabled())
        mapView_.getController().zoomOut();
      return true;
    } // if ...

    return false;
  } // tapPrevNext
} // class ZoomButtonsOverlay
