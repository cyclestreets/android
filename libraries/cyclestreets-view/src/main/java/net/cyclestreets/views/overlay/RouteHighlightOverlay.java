package net.cyclestreets.views.overlay;

import net.cyclestreets.view.R;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

public class RouteHighlightOverlay extends Overlay implements ButtonTapListener
{
  private final CycleMapView mapView;

  private Segment current;

  private final FloatingActionButton prevButton;
  private final FloatingActionButton nextButton;

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

    View routeHighlightView = LayoutInflater.from(mapView.getContext()).inflate(R.layout.route_highlight_buttons, null);

    prevButton = routeHighlightView.findViewById(R.id.route_highlight_prev);
    prevButton.setVisibility(View.INVISIBLE);
    prevButton.setOnClickListener(view -> regressActiveSegment(1));
    prevButton.setOnLongClickListener(view -> regressActiveSegment(Integer.MAX_VALUE));

    nextButton = routeHighlightView.findViewById(R.id.route_highlight_next);
    nextButton.setVisibility(View.INVISIBLE);
    nextButton.setOnClickListener(view -> advanceActiveSegment(1));
    nextButton.setOnLongClickListener(view -> advanceActiveSegment(Integer.MAX_VALUE));
    mapView.addView(routeHighlightView);

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
    Log.i("drawButtons", "1");
    if (!Route.available()) {
      prevButton.hide();
      nextButton.hide();
      return;
    }

    drawSegmentInfo(canvas);

    Log.i("drawButtons", "atStart: " + Route.journey().atStart());
    Log.i("drawButtons", "atEnd: " + Route.journey().atEnd());
    prevButton.setEnabled(!Route.journey().atStart());
    prevButton.show();
    nextButton.setEnabled(!Route.journey().atEnd());
    nextButton.show();
  }

  private void drawSegmentInfo(final Canvas canvas) {
    Log.i("drawSegmentInfo", "1");
    final Segment seg = Route.journey().activeSegment();
    if (seg == null)
      return;
    Log.i("drawSegmentInfo", "2");

    final Rect box = canvas.getClipBounds();
    box.bottom = box.top + 72;

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
    // We're handling button taps via an onClickListener instead.
    return false;
  }

  @Override
  public boolean onButtonDoubleTap(MotionEvent event) {
    // We're handling button taps via an onClickListener instead.
    return false;
  }

  private boolean regressActiveSegment(int stepsToMove) {
    Log.i("highlight", "Regressing by " + stepsToMove);
    if (!Route.available()) {
      Log.i("highlight", "no route");
      return false;
    }

    for (int i = stepsToMove; i > 0 && !Route.journey().atStart(); i--)
      Route.journey().regressActiveSegment();

    mapView.invalidate();
    return true;
  }

  private boolean advanceActiveSegment(int stepsToMove) {
    if (!Route.available())
      return false;

    for (int i = stepsToMove; i > 0 && !Route.journey().atEnd(); i--)
      Route.journey().advanceActiveSegment();
    mapView.invalidate();
    return true;
  }

}
