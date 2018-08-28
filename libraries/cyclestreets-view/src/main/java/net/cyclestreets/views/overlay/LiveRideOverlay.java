package net.cyclestreets.views.overlay;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.liveride.LiveRideService;
import net.cyclestreets.routing.DistanceFormatter;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import net.cyclestreets.util.TurnIcons;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.IBinder;
import android.view.View;

public class LiveRideOverlay extends Overlay implements ServiceConnection
{
  private final Activity activity_;
  private LiveRideService.Binding binding_;
  private final int offset_;
  private final float radius_;
  private final Paint largeTextBrush_;
  private final Paint midTextBrush_;
  private final Paint smallTextBrush_;
  private final Paint fillBrush_;
  private final int speedWidth_;
  private final int kmWidth_;
  private final int lineHeight_;
  private final DistanceFormatter formatter_;

  public LiveRideOverlay(final Activity context, final View view) {
    super();

    activity_ = context;

    final Intent intent = new Intent(activity_, LiveRideService.class);
    activity_.bindService(intent, this, Context.BIND_AUTO_CREATE);

    offset_ = DrawingHelper.offset(context);
    radius_ = DrawingHelper.cornerRadius(context);
    largeTextBrush_ = Brush.createTextBrush(offset_*4);
    largeTextBrush_.setTextAlign(Align.LEFT);
    midTextBrush_ = Brush.createTextBrush(offset_*2);
    midTextBrush_.setTextAlign(Align.LEFT);
    smallTextBrush_ = Brush.createTextBrush(offset_);
    smallTextBrush_.setTextAlign(Align.LEFT);
    fillBrush_ = Brush.HighlightBrush(context);

    formatter_ = DistanceFormatter.formatter(CycleStreetsPreferences.units());

    speedWidth_ = (int)largeTextBrush_.measureText("0.0");
    kmWidth_ = (int)midTextBrush_.measureText(formatter_.speedUnit());

    final Rect bounds = new Rect();
    largeTextBrush_.getTextBounds("0.0", 0, 3, bounds); // Measure the text
    lineHeight_ = bounds.height();
  }

  @Override
  public void onDetach(final MapView mapView) {
    if (binding_ != null)
      binding_.stopRiding();
    activity_.unbindService(this);

    super.onDetach(mapView);
  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    final Matrix unscaled = mapView.getProjection().getInvertedScaleRotateCanvasMatrix();

    canvas.save();
    canvas.concat(unscaled);

    try {
      drawNextTurn(canvas);
      drawSpeed(canvas);
    } catch (Exception e) {
    }

    canvas.restore();
  }

  private void drawNextTurn(final Canvas canvas) {
    final Rect box = canvas.getClipBounds();
    int eighth = box.width() / 8;

    box.right = box.left + (eighth * 2);
    box.bottom = box.top + (eighth * 2);

    drawThenShrink(canvas, box, fillBrush_);
    drawThenShrink(canvas, box, Brush.White);

    final Segment nextSeg = Route.journey().nextSegment();
    final Drawable turnIcon = TurnIcons.icon(nextSeg.turn());
    turnIcon.setBounds(box);
    turnIcon.draw(canvas);

    if (Route.journey().atStart())
      return;

    final String distanceTo = distanceUntilTurn();
    final String nextStreet = nextSeg.street();

    final Rect distanceToBox = canvas.getClipBounds();
    distanceToBox.left = box.right + (offset_*2);
    distanceToBox.bottom = distanceToBox.top + offset_;
    int bottom = Draw.measureTextInRect(canvas, midTextBrush_, distanceToBox, distanceTo);
    distanceToBox.bottom = bottom + offset_;

    final Rect nextBox = new Rect(distanceToBox);
    nextBox.top = distanceToBox.bottom;
    nextBox.bottom = nextBox.top + offset_;
    bottom = Draw.measureTextInRect(canvas, smallTextBrush_, nextBox, nextStreet);
    nextBox.bottom = bottom + offset_;

    final Rect wrapperBox = new Rect(distanceToBox);
    wrapperBox.bottom = nextBox.bottom;

    DrawingHelper.drawRoundRect(canvas, wrapperBox, radius_, fillBrush_);
    Draw.drawTextInRect(canvas, midTextBrush_, distanceToBox, distanceTo);
    Draw.drawTextInRect(canvas, smallTextBrush_, nextBox, nextStreet);

    turnIcon.draw(canvas);
  }

  private void drawThenShrink(final Canvas canvas, final Rect box, final Paint brush) {
    DrawingHelper.drawRoundRect(canvas, box, radius_, brush);

    box.left += offset_;
    box.right -= offset_;
    box.top += offset_;
    box.bottom -= offset_;
  }

  private void drawSpeed(final Canvas canvas) {
    final String speed = speed();

    final int fullWidth_ = speedWidth_ + kmWidth_;

    final Rect box = canvas.getClipBounds();
    box.right = box.left + fullWidth_ + (offset_*2);
    box.top = box.bottom - (lineHeight_ + offset_*2);

    DrawingHelper.drawRoundRect(canvas, box, radius_, fillBrush_);

    box.left += offset_;
    box.bottom -= offset_;

    canvas.drawText(speed, box.left, box.bottom, largeTextBrush_);
    box.left += speedWidth_;
    canvas.drawText(formatter_.speedUnit(), box.left, box.bottom, midTextBrush_);
  }

  ///////////////////////////
  @Override
  public void onServiceConnected(final ComponentName className, final IBinder binder) {
    binding_ = (LiveRideService.Binding)binder;

    if (!binding_.areRiding())
      binding_.startRiding();
  }

  @Override
  public void onServiceDisconnected(final ComponentName className) {
  }

  private Location lastLocation() {
    if (!Route.available())
      return null;

    if (binding_ == null)
      return null;

    final Location location = binding_.lastLocation();
    if (location == null)
      return null;

    return location;
  }

  private String speed() {
    final Location location = lastLocation();
    if (location == null)
      return "0.0";

    return formatter_.speed(location.getSpeed());
  }

  private String distanceUntilTurn() {
    final Location location = lastLocation();
    if (location == null)
      return "";

    final GeoPoint whereIam = new GeoPoint(location);
    final Segment activeSeg = Route.journey().activeSegment();
    final int fromEnd = activeSeg.distanceFromEnd(whereIam);

    return formatter_.distance(fromEnd);
  }
}
