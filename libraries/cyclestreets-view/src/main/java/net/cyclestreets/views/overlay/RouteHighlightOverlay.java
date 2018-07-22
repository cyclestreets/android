package net.cyclestreets.views.overlay;

import net.cyclestreets.view.R;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import net.cyclestreets.util.GPS;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class RouteHighlightOverlay extends Overlay implements ButtonTapListener
{
  private final CycleMapView mapView;

  private Segment current;

  private final OverlayButton prevButton;
  private final OverlayButton nextButton;

  private final int offset;
  private final float radius;

  private final Paint fillBrush;
  private final Paint textBrush;

  public RouteHighlightOverlay(final Context context, final CycleMapView map) {
    super();

    mapView = map;
    current = null;

    offset = DrawingHelper.offset(context);
    radius = DrawingHelper.cornerRadius(context);

    final Resources res = context.getResources();
    prevButton = new OverlayButton(res.getDrawable(R.drawable.btn_previous),
            offset,
                                    offset *2,
            radius);
    prevButton.bottomAlign();
    nextButton = new OverlayButton(res.getDrawable(R.drawable.btn_next),
                                    prevButton.right() + offset,
                                    offset *2,
            radius);
    nextButton.bottomAlign();

    textBrush = Brush.createTextBrush(offset);
    textBrush.setTextAlign(Align.LEFT);
    fillBrush = Brush.HighlightBrush(context);
  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    if (current == Route.journey().activeSegment())
      return;

    current = Route.journey().activeSegment();
    if (current == null)
      return;

    this.mapView.getController().animateTo(current.start());
  }

  @Override
  public void drawButtons(final Canvas canvas, final MapView mapView) {
    if (!Route.available())
      return;

    drawSegmentInfo(canvas);

    prevButton.enable(!Route.journey().atStart());
    prevButton.draw(canvas);
    nextButton.enable(!Route.journey().atEnd());
    nextButton.draw(canvas);
  }

  private void drawSegmentInfo(final Canvas canvas) {
    final Segment seg = Route.journey().activeSegment();
    if (seg == null)
      return;

    final Rect box = canvas.getClipBounds();
    box.bottom = box.top + prevButton.height();

    final Rect textBox = new Rect(box);
    textBox.left += offset;
    textBox.right -= offset;
    int bottom = Draw.measureTextInRect(canvas, textBrush, textBox, seg.toString());

    if (bottom >= box.bottom)
      box.bottom = bottom + offset;

    DrawingHelper.drawRoundRect(canvas, box, radius, fillBrush);
    Draw.drawTextInRect(canvas, textBrush, textBox, seg.toString());
  }

  //////////////////////////////////////////////
  @Override
  public boolean onButtonTap(final MotionEvent event) {
    if (!Route.available())
      return false;

    if (!prevButton.hit(event) && !nextButton.hit(event))
      return false;

    if (prevButton.hit(event))
      Route.journey().regressActiveSegment();

    if (nextButton.hit(event))
      Route.journey().advanceActiveSegment();

    mapView.invalidate();
    return true;
  }

  public boolean onButtonDoubleTap(final MotionEvent event) {
    if (!Route.available())
      return false;

    if (!prevButton.hit(event) && !nextButton.hit(event))
      return false;

    if (prevButton.hit(event))
      while (!Route.journey().atStart())
        Route.journey().regressActiveSegment();

    if (nextButton.hit(event))
      while (!Route.journey().atEnd())
        Route.journey().advanceActiveSegment();

    mapView.invalidate();
    return true;
  }
}
