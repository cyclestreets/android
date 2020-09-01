package net.cyclestreets.views.overlay;

import net.cyclestreets.CycleStreetsPreferences;
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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.location.Location;

public class LiveRideOverlay extends Overlay
{
  public interface Locator {
    Location lastLocation();
  }

  private final Locator locator_;
  private final int offset_;
  private final float radius_;
  private final Paint midTextBrush_;
  private final Paint smallTextBrush_;
  private final Paint fillBrush_;

  private final DistanceFormatter formatter_;
  private Integer distanceUntilTurn;

  private LiveRideJourney liveRideJourney;

  public LiveRideOverlay(final Activity context, final Locator locator) {
    super();

    locator_ = locator;
    offset_ = DrawingHelperKt.offset(context);
    radius_ = DrawingHelperKt.cornerRadius();
    midTextBrush_ = Brush.createTextBrush((int)(offset_ * 2));
    midTextBrush_.setTextAlign(Align.LEFT);
    smallTextBrush_ = Brush.createTextBrush(offset_);
    smallTextBrush_.setTextAlign(Align.LEFT);
    fillBrush_ = Brush.HighlightBrush(context);

    formatter_ = DistanceFormatter.formatter(CycleStreetsPreferences.units());

    final Rect bounds = new Rect();

    smallTextBrush_.getTextBounds("0.0", 0, 3, bounds);

    liveRideJourney = new LiveRideJourney(context);

  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    final Matrix unscaled = mapView.getProjection().getInvertedScaleRotateCanvasMatrix();

    canvas.save();
    canvas.concat(unscaled);

    final Location location = lastLocation();

    try {
      distanceUntilTurn = distanceUntilTurn(location);
      drawNextTurn(canvas);
      liveRideJourney.drawJourneyInfo(canvas, distanceUntilTurn, location);
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

    final String distanceTo;
    if (distanceUntilTurn == null)
      distanceTo = "";
    else
      distanceTo = formatter_.distance(distanceUntilTurn);

    final String nextStreet = nextSeg.street();

    final Rect distanceToBox = canvas.getClipBounds();
    distanceToBox.left = box.right + (offset_ * 2);
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

    DrawingHelperKt.drawRoundRect(canvas, wrapperBox, radius_, fillBrush_);
    Draw.drawTextInRect(canvas, midTextBrush_, distanceToBox, distanceTo);
    Draw.drawTextInRect(canvas, smallTextBrush_, nextBox, nextStreet);

    turnIcon.draw(canvas);
  }

  private void drawThenShrink(final Canvas canvas, final Rect box, final Paint brush) {
    DrawingHelperKt.drawRoundRect(canvas, box, radius_, brush);

    box.left += offset_;
    box.right -= offset_;
    box.top += offset_;
    box.bottom -= offset_;
  }

  private Location lastLocation() {
    if (!Route.routeAvailable())
      return null;

    if (locator_ == null)
      return null;

    final Location location = locator_.lastLocation();
    if (location == null)
      return null;

    return location;
  }

  private Integer distanceUntilTurn(Location location) {

    if (location != null) {
      // Get distance to end of active segment
      final Segment activeSeg = Route.journey().activeSegment();
      final GeoPoint whereIAm = new GeoPoint(location);
      if (activeSeg != null)
        return activeSeg.distanceFromEnd(whereIAm);
    }
    return null;
  }

}
